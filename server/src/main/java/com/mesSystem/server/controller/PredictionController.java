package com.mesSystem.server.controller;

import com.mesSystem.server.model.AnalysisReport;
import com.mesSystem.server.model.Patient;
import com.mesSystem.server.service.AnalysisReportService;
import com.mesSystem.server.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 预测服务控制器
 * 
 * @author Tlt
 * @date 2024/1/6
 */
@RestController
@RequestMapping("/prediction")
public class PredictionController {

    private final RestTemplate restTemplate;
    private final PatientService patientService;
    private final AnalysisReportService analysisReportService;
   private final String predictionServiceUrl = "http://localhost:5000/predict";
    // private final String predictionServiceUrl = "https://83768c45t7.goho.co/predict";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public PredictionController(
            RestTemplate restTemplate,
            PatientService patientService,
            AnalysisReportService analysisReportService) {
        this.restTemplate = restTemplate;
        this.patientService = patientService;
        this.analysisReportService = analysisReportService;
    }

    /**
     * 调用预测服务接口并保存分析报告
     * 
     * @param requestBody 请求体，包含患者ID、图片数据、分析类型和体质问卷数据
     * @return 预测服务返回的结果，合并体质调查结果
     */
    @PostMapping
    public ResponseEntity<Object> predict(@RequestBody Map<String, Object> requestBody) {
        try {
            // 获取请求参数
            String patientId = (String) requestBody.get("patientId");
            String imageData = (String) requestBody.get("imageData");
            String reportType = (String) requestBody.get("reportType");
            Map<String, Object> questionnaire = (Map<String, Object>) requestBody.get("questionnaire");
            
            // 验证必要参数
            if (patientId == null || imageData == null || reportType == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "参数错误",
                    "message", "patientId、imageData和reportType为必填项"
                ));
            }
            
            // 查找患者
            Patient patient = patientService.findById(patientId);
            if (patient == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "患者不存在",
                    "message", "未找到ID为" + patientId + "的患者"
                ));
            }
            
            // 处理图片数据
            String base64Image = imageData;
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            
            // 创建 multipart/form-data 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // 创建表单数据
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            };
            formData.add("image", imageResource);
            
            // 创建请求实体
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);
            
            // 发送请求并获取响应
            ResponseEntity<Map> response = restTemplate.exchange(
                predictionServiceUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            // 获取预测结果
            Map<String, Object> predictionResult = response.getBody();
            
            // 创建分析报告
            AnalysisReport report = new AnalysisReport();
            report.setId(UUID.randomUUID().toString());
            report.setPatient(patient);
            report.setReportType(reportType.toUpperCase());
            report.setOriginalImage(imageBytes);
            
            // 将预测结果转换为字符串并保存
            if (predictionResult != null) {
                // 如果返回的是分析后的图片，保存到分析图片字段
                if (predictionResult.containsKey("image_base64")) {
                    String resultImageBase64 = (String) predictionResult.get("image_base64");
                    if (resultImageBase64.contains(",")) {
                        resultImageBase64 = resultImageBase64.split(",")[1];
                    }
                    report.setAnalysisImage(Base64.getDecoder().decode(resultImageBase64));
                }
                
                // 从inference_result中提取舌诊分析结果
                if (predictionResult.containsKey("inference_result")) {
                    List<String> inferenceResult = (List<String>) predictionResult.get("inference_result");
                    // 构建分析结果JSON对象
                    Map<String, String> analysisResultMap = new HashMap<>();
                    
                    // 根据inference_result的格式设置对应的字段
                    if (inferenceResult.size() >= 6) {
                        analysisResultMap.put("coating", inferenceResult.get(0)); // 苔薄白
                        analysisResultMap.put("color", inferenceResult.get(1)); // 颜色，可能为空数组 "[]"
                        analysisResultMap.put("cracks", inferenceResult.get(2)); // 裂纹
                        analysisResultMap.put("ecchymosis", inferenceResult.get(3)); // 瘀斑
                        analysisResultMap.put("color2", inferenceResult.get(4)); // 颜色2，可能为空数组 "[]"
                        analysisResultMap.put("toothMarks", inferenceResult.get(5)); // 齿痕
                    }
                    
                    // 将分析结果转换为JSON字符串并保存
                    report.setAnalysisResult(objectMapper.writeValueAsString(analysisResultMap));
                } else {
                    // 如果没有inference_result字段，保存整个预测结果
                    report.setAnalysisResult(objectMapper.writeValueAsString(predictionResult));
                }
            }
            
            // 处理体质调查问卷结果
            if (questionnaire != null) {
                // 如果前端未评判体质，在后端重新评判体质
                if (!questionnaire.containsKey("mainType") || !questionnaire.containsKey("secondaryType")) {
                    Map<String, Object> constitutionResult = evaluateConstitution(questionnaire);
                    questionnaire.putAll(constitutionResult);
                }
                
                // 保存体质调查结果
                report.setConstitutionResult(objectMapper.writeValueAsString(questionnaire));
                
                // 提取主要体质和次要体质类型
                if (questionnaire.containsKey("mainType")) {
                    report.setMainConstitutionType((String) questionnaire.get("mainType"));
                }
                
                if (questionnaire.containsKey("secondaryType")) {
                    report.setSecondaryConstitutionType((String) questionnaire.get("secondaryType"));
                }
            }
            
            // 保存分析报告
            analysisReportService.save(report);
            
            // 只返回保存成功的消息
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "分析报告保存成功",
                "reportId", report.getId()
            ));
            
        } catch (Exception e) {
            // 记录错误日志
            System.err.println("调用预测服务失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "调用预测服务失败",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 评估中医体质类型
     * 根据问卷回答判断体质类型
     * 
     * @param questionnaire 前端提交的问卷数据
     * @return 评估结果，包含主要体质和次要体质
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> evaluateConstitution(Map<String, Object> questionnaire) {
        Map<String, Object> result = new HashMap<>();
        
        // 默认为平和质
        String mainType = "平和质";
        String secondaryType = "平和质";
        
        try {
            // 获取问卷答案
            List<Map<String, Object>> answers = (List<Map<String, Object>>) questionnaire.get("answers");
            if (answers == null || answers.isEmpty()) {
                result.put("mainType", mainType);
                result.put("secondaryType", secondaryType);
                return result;
            }
            
            // 统计各体质类型的D、E答案数量
            Map<String, Integer> typeCountsDE = new HashMap<>();
            
            // 初始化各类型计数
            String[] allTypes = {"气虚质", "阳虚质", "血虚质", "阴虚质", "痰湿质", "湿热质", "血淤质", "气郁质", "平和质"};
            for (String type : allTypes) {
                typeCountsDE.put(type, 0);
            }
            
            // 计算各类型D、E答案的数量
            for (Map<String, Object> answer : answers) {
                String answerValue = (String) answer.get("answer");
                String type = (String) answer.get("type");
                
                if (answerValue != null && (answerValue.equals("D") || answerValue.equals("E"))) {
                    typeCountsDE.put(type, typeCountsDE.get(type) + 1);
                }
            }
            
            // 判断是否为平和质（所有题都选A）
            boolean isBalanced = true;
            for (Map<String, Object> answer : answers) {
                String answerValue = (String) answer.get("answer");
                if (answerValue == null || !answerValue.equals("A")) {
                    isBalanced = false;
                    break;
                }
            }
            
            if (isBalanced) {
                mainType = "平和质";
            } else {
                // 判断主要体质类型
                // 气虚质：1、2题均选D或E
                if (typeCountsDE.get("气虚质") == 2) {
                    mainType = "气虚质";
                    
                    // 气虚伴阳虚质：1、2、3、4题都为D或E
                    if (typeCountsDE.get("阳虚质") == 2) {
                        secondaryType = "阳虚质";
                    }
                } 
                // 判断其他体质类型
                else {
                    // 根据D、E频率最高的确定主体质
                    int maxCount = 0;
                    for (Map.Entry<String, Integer> entry : typeCountsDE.entrySet()) {
                        String type = entry.getKey();
                        int count = entry.getValue();
                        
                        if (!type.equals("平和质") && count > maxCount) {
                            maxCount = count;
                            mainType = type;
                        }
                    }
                    
                    // 寻找次高频率的类型作为次要体质
                    int secondMaxCount = 0;
                    for (Map.Entry<String, Integer> entry : typeCountsDE.entrySet()) {
                        String type = entry.getKey();
                        int count = entry.getValue();
                        
                        if (!type.equals(mainType) && !type.equals("平和质") && count > secondMaxCount) {
                            secondMaxCount = count;
                            secondaryType = type;
                        }
                    }
                }
            }
            
            result.put("mainType", mainType);
            result.put("secondaryType", secondaryType != null ? secondaryType : mainType);
            result.put("constitutionCounts", typeCountsDE);
            result.put("isBalanced", isBalanced);
            
        } catch (Exception e) {
            System.err.println("体质评判错误: " + e.getMessage());
            result.put("mainType", mainType);
            result.put("secondaryType", secondaryType);
            result.put("error", "体质评判失败: " + e.getMessage());
        }
        
        return result;
    }
}