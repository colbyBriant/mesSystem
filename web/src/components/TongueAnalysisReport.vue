<template>
  <div class="tongue-analysis-report bg-gray-50 p-8">
    <div ref="reportRef" class="bg-white p-6 rounded-lg border border-gray-200 shadow-sm mx-auto" style="max-width: 800px;">
      
      <!-- 患者信息部分 -->
      <div class="mb-4 pb-4 border-b border-gray-200">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">患者姓名</div>
            <div class="text-sm text-gray-800">{{ patient?.name || '未知' }}</div>
          </div>
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">检测时间</div>
            <div class="flex items-center">
              <span class="text-sm text-gray-800 mr-2">{{ formatDate(currentReport?.createTime) }}</span>
              <!-- 历史报告选择器移到时间右边 -->
              <el-select 
                v-if="historyReports.length > 0"
                v-model="selectedReportId" 
                placeholder="选择历史报告" 
                size="small"
                @change="handleReportChange"
                class="history-report-select"
              >
                <el-option 
                  v-for="report in historyReports" 
                  :key="report.id" 
                  :label="formatDate(report.createTime)" 
                  :value="report.id"
                />
              </el-select>
            </div>
            
            <!-- 移除原来的历史报告选择器 -->
          </div>
        </div>
      </div>
      
      <!-- 舌诊图像分析卡片 -->
      <div class="grid grid-cols-2 gap-5 mb-6">
        <!-- 原始采集图像 -->
        <div class="text-center">
          <div class="flex-1 flex items-center justify-center w-full mb-2">
            <div class="w-40 h-40 bg-gray-50 rounded-sm border border-gray-100 flex items-center justify-center overflow-hidden cursor-pointer tongue-image-container"
              @click="openImageViewer(analysisReport?.originalImage || patient?.tongueImage || '')">
              <img 
                :src="analysisReport?.originalImage || patient?.tongueImage || ''" 
                alt="原始舌头图像"
                class="max-w-full max-h-full object-contain hover:opacity-90 transition-opacity"
                v-if="analysisReport?.originalImage || patient?.tongueImage"
              />
              <span class="text-gray-400 text-sm" v-else>暂无图像</span>
            </div>
          </div>
          <div class="text-sm">原始采集图像</div>
        </div>
        
        <!-- 舌头分割图像 -->
        <div class="text-center">
          <div class="flex-1 flex items-center justify-center w-full mb-2">
            <div class="w-40 h-40 bg-gray-50 rounded-sm border border-gray-100 flex items-center justify-center overflow-hidden cursor-pointer tongue-image-container"
              @click="openImageViewer(analysisReport?.analysisImage || patient?.tongueMaskImage || '')">
              <img 
                :src="analysisReport?.analysisImage || patient?.tongueMaskImage || ''" 
                alt="舌头分割图像"
                class="max-w-full max-h-full object-contain hover:opacity-90 transition-opacity"
                v-if="analysisReport?.analysisImage || patient?.tongueMaskImage"
              />
              <span class="text-gray-400 text-sm" v-else>暂无图像</span>
            </div>
          </div>
          <div class="text-sm">舌头分割图像</div>
        </div>
      </div>
        
      <!-- 舌诊分析结果区域 -->
      <div class="mb-6">
        <h3 class="font-medium mb-3 pb-1 border-b border-gray-200">
          舌诊分析结果
        </h3>
        
        <!-- 舌诊分析结果表格 -->
        <div class="grid grid-cols-3 gap-4">
          <!-- 舌苔 -->
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">舌苔</div>
            <div class="text-sm text-gray-600">
              {{ analysisResult?.coating || patient?.tongueAnalysis?.coating || '正常' }}
            </div>
          </div>
          
          <!-- 舌色 -->
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">舌色</div>
            <div class="text-sm text-gray-600">
              {{ formatDisplayValue(analysisResult?.color) || '正常' }}
            </div>
          </div>
          
          <!-- 质地 -->
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">质地</div>
            <div class="text-sm text-gray-600">
              {{ formatDisplayValue(analysisResult?.color2) || '正常' }}
            </div>
          </div>
          
          <!-- 瘀斑 -->
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">瘀斑</div>
            <div class="text-sm text-gray-600">
              {{ analysisResult?.ecchymosis || patient?.tongueAnalysis?.ecchymosis || '无' }}
            </div>
          </div>
          
          <!-- 裂纹 -->
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">裂纹</div>
            <div class="text-sm text-gray-600">
              {{ analysisResult?.cracks || patient?.tongueAnalysis?.cracks || '无' }}
            </div>
          </div>
          
          <!-- 齿痕 -->
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">齿痕</div>
            <div class="text-sm text-gray-600">
              {{ analysisResult?.toothMarks || patient?.tongueAnalysis?.toothMarks || '无' }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 体质辨识结果区域 -->
      <div class="mb-6">
        <h3 class="font-medium mb-3 pb-1 border-b border-gray-200">
          体质辨识结果
        </h3>
        
        <div class="grid grid-cols-2 gap-6 mb-4">
          <!-- 主要体质类型 -->
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">主要体质类型</div>
            <div class="text-blue-600 text-sm">
              {{ analysisReport?.mainConstitutionType || patient?.constitutionAnalysis?.mainType || '气虚质' }}
            </div>
          </div>
          
          <!-- 次要体质类型 -->
          <div>
            <div class="font-medium mb-1 text-gray-700 text-sm">次要体质类型</div>
            <div class="text-blue-600 text-sm">
              {{ analysisReport?.secondaryConstitutionType || patient?.constitutionAnalysis?.secondaryType || '阴虚质' }}
            </div>
          </div>
        </div>
        
        <!-- 体质特征说明 -->
        <div>
          <div class="font-medium mb-1 text-gray-700 text-sm">体质特征说明</div>
          <div class="text-sm text-gray-600">
            {{ getConstitutionDescription(analysisReport?.mainConstitutionType || patient?.constitutionAnalysis?.mainType) }}
          </div>
        </div>
      </div>
      
      <!-- 底部按钮 -->
      <div class="flex justify-center mt-8 space-x-4 border-t border-gray-200 pt-4">
        <button
          class="px-6 py-2 bg-blue-500 text-white rounded-sm cursor-pointer hover:bg-blue-600"
          @click="handleSaveReport"
        >
          保存结果
        </button>
        <button
          class="border border-gray-300 px-6 py-2 rounded-sm cursor-pointer hover:bg-gray-50"
          @click="$emit('close')"
        >
          返回
        </button>
      </div>
    </div>
    
    <!-- 图像查看器 -->
    <div class="image-viewer" v-if="showImageViewer" @click="closeImageViewer">
      <div class="image-viewer-content" @click.stop>
        <img :src="viewerImage" alt="图像查看" class="viewer-img" />
        <button class="close-button" @click="closeImageViewer">&times;</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted, computed, watch } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import { Document, DataLine, User, CameraFilled } from "@element-plus/icons-vue";
import request from '@/utils/request';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';

const props = defineProps({
  patient: {
    type: Object,
    required: true
  },
  reportData: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close', 'save']);

// 添加分析报告数据
const analysisReport = ref(null);
const loading = ref(false);
const currentReport = ref(null);

// 添加历史报告相关数据
const historyReports = ref([]);
const selectedReportId = ref('');

// 添加图像查看相关数据
const showImageViewer = ref(false);
const viewerImage = ref('');

// 获取历史分析报告
const fetchHistoryReports = async (patientId) => {
  if (!patientId) return;
  
  try {
    loading.value = true;
    
    // 从后端数据库获取特定患者的舌象分析报告
    const response = await request.get(`/analysis-reports/patient/${patientId}`, {
      params: { 
        reportType: 'TONGUE',
        // 可以添加额外的参数，如排序规则等
        // sort: 'createTime,desc'
      }
    });
    
    if (Array.isArray(response)) {
      // 按照创建时间降序排序，最新的在前
      historyReports.value = response.sort((a, b) => {
        return new Date(b.createTime) - new Date(a.createTime);
      });
      
      console.log('获取到历史分析报告:', historyReports.value.length, '条');
      
      // 如果有历史报告但没有当前选中的报告，自动选择最新的一条
      if (historyReports.value.length > 0 && !selectedReportId.value) {
        selectedReportId.value = historyReports.value[0].id;
        analysisReport.value = historyReports.value[0];
        currentReport.value = historyReports.value[0];
      }
    } else {
      historyReports.value = [];
      console.warn('未找到历史分析报告');
    }
  } catch (error) {
    console.error('获取历史分析报告失败:', error);
    ElMessage.error('获取历史报告失败: ' + (error.message || '服务器错误'));
    historyReports.value = [];
  } finally {
    loading.value = false;
  }
};

// 监听reportData变化
watch(() => props.reportData, (newReportData) => {
  if (newReportData) {
    analysisReport.value = newReportData;
    currentReport.value = newReportData;
    
    // 如果有新的报告数据，设置当前选中的报告ID
    if (newReportData.id) {
      selectedReportId.value = newReportData.id;
    }
    
    // 如果有患者ID，获取历史报告
    if (props.patient?.id) {
      fetchHistoryReports(props.patient.id);
    }
  }
}, { immediate: true });

// 监听patient变化
watch(() => props.patient?.id, (newPatientId) => {
  if (newPatientId) {
    fetchHistoryReports(newPatientId);
  }
});

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知日期';
  
  try {
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    console.error('日期格式化错误:', e);
    return dateString;
  }
};

// 从分析结果字符串中解析出舌诊分析结果对象
const analysisResult = computed(() => {
  if (!analysisReport.value || !analysisReport.value.analysisResult) {
    return null;
  }
  
  try {
    return JSON.parse(analysisReport.value.analysisResult);
  } catch (error) {
    console.error('解析分析结果出错:', error);
    return null;
  }
});

// 处理报告切换
const handleReportChange = async (reportId) => {
  if (!reportId) return;
  
  try {
    loading.value = true;
    
    // 查找选中的报告
    const selectedReport = historyReports.value.find(report => report.id === reportId);
    
    if (selectedReport) {
      // 直接使用列表中的报告数据
      analysisReport.value = selectedReport;
      currentReport.value = selectedReport;
    } else {
      // 如果在列表中没有找到，从服务器获取
      const response = await request.get(`/analysis-reports/${reportId}`);
      
      if (response) {
        analysisReport.value = response;
        currentReport.value = response;
      } else {
        ElMessage.warning('无法获取所选报告');
      }
    }
  } catch (error) {
    console.error('切换报告失败:', error);
    ElMessage.error('切换报告失败');
  } finally {
    loading.value = false;
  }
};

// 获取最新分析报告
const fetchLatestReport = async () => {
  if (!props.patient?.id) {
    return;
  }
  
  try {
    loading.value = true;
    
    const response = await request.get(`/patients/${props.patient.id}/reports/latest?type=TONGUE`);
    
    if (response && response.id) {
      analysisReport.value = response;
      currentReport.value = response;
      selectedReportId.value = response.id;
      console.log('获取到分析报告:', response);
    }
    
    // 获取历史报告
    await fetchHistoryReports(props.patient.id);
  } catch (error) {
    console.error('获取分析报告失败:', error);
    ElMessage.error('获取分析报告失败');
  } finally {
    loading.value = false;
  }
};

// 获取体质描述的辅助函数
const getConstitutionDescription = (constitutionType) => {
  const descriptions = {
    '平和质': '平和质表示体质平衡，阴阳调和，气血充沛，体形适中，面色红润，精力充沛，睡眠良好，适应力强，抗病能力强。平和体质应保持良好的生活习惯和健康饮食，进行适度运动。',
    '气虚质': '气虚质的人容易疲劳，气短懒言，动则气喘，精神不振，面色白，说话声音低，注意力不集中。适当调整饮食，多吃补气食物，适应运动，避免过度劳累。',
    '阳虚质': '阳虚质的人畏寒怕冷，手脚凉，精神不振，喜热饮食，面色偏白，容易感冒。保持温暖，避免寒冷环境，可适当食用温阳食物，进行适度锻炼。',
    '血虚质': '血虚质的人容易面色苍白，头晕眼花，指甲色淡，唇色淡白，睡眠不佳。注意保持良好的饮食习惯，多吃富含铁和蛋白质的食物，保持充足睡眠。',
    '阴虚质': '阴虚质的人容易手脚心热，口干咽干，皮肤干燥，大便干结，舌苔少。注意保持足够的水分摄入，避免辛辣刺激性食物，注意休息，减少熬夜。',
    '痰湿质': '痰湿质的人体形肥胖，腹部松软，容易疲乏，舌苔厚腻，面部皮肤油腻。注意控制饮食，少吃高脂肪、高糖分食物，多运动，保持规律生活。',
    '湿热质': '湿热质的人容易口苦口干，大便粘腻不爽，小便短黄，面色偏黄或油光。减少辛辣刺激性食物摄入，多喝水，保持良好的生活习惯，加强锻炼。',
    '血淤质': '血淤质的人面色晦暗，容易出现瘀斑，疼痛部位固定，肌肤甲错，舌质紫暗或有瘀点。调整饮食结构，避免过度劳累，保持心情舒畅，适当运动。',
    '气郁质': '气郁质的人性格内向，情绪波动大，容易忧虑，胸胁或腹部胀痛，舌苔薄白。保持心情愉悦，适当进行情绪疏导，参加社交活动，培养兴趣爱好。',
    '特禀质': '特禀质的人容易过敏，对某些物质特别敏感，需要避免接触过敏原，加强体质锻炼，提高免疫力。'
  };
  
  return descriptions[constitutionType] || '请根据中医师建议，进行针对性的调理和保健。';
};

// 报告容器的引用
const reportRef = ref(null);

// 保存报告方法
const handleSaveReport = async () => {
  if (!props.patient || !props.patient.id) {
    ElMessage.warning('请先选择患者');
    return;
  }
  
  try {
    // 显示加载提示
    const loading = ElLoading.service({
      lock: true,
      text: '正在生成报告...',
      background: 'rgba(0, 0, 0, 0.7)'
    });
    
    if (!reportRef.value) {
      loading.close();
      ElMessage.error('无法获取报告内容');
      return;
    }
    
    // 使用html2canvas将报告转换为图片
    const canvas = await html2canvas(reportRef.value, {
      scale: 2, // 提高图片质量
      useCORS: true, // 允许加载跨域图片
      logging: false,
      allowTaint: true,
      backgroundColor: '#ffffff'
    });
    
    // 创建PDF文档
    const imgData = canvas.toDataURL('image/jpeg', 0.8);
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4'
    });
    
    // 计算PDF宽高
    const imgWidth = 210; // A4纸宽度
    const pageHeight = 297; // A4纸高度
    const imgHeight = (canvas.height * imgWidth) / canvas.width;
    let heightLeft = imgHeight;
    let position = 0;
    
    // 添加第一页
    pdf.addImage(imgData, 'JPEG', 0, position, imgWidth, imgHeight);
    heightLeft -= pageHeight;
    
    // 如果内容超过一页，添加更多页面
    while (heightLeft > 0) {
      position = heightLeft - imgHeight;
      pdf.addPage();
      pdf.addImage(imgData, 'JPEG', 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;
    }
    
    // 生成文件名
    const patientName = props.patient.name || '患者';
    const reportDate = new Date().toISOString().split('T')[0];
    const fileName = `${patientName}_舌诊分析报告_${reportDate}.pdf`;
    
    // 下载PDF
    pdf.save(fileName);
    
    // 关闭加载提示
    loading.close();
    
    // 触发保存事件，由父组件处理额外的保存逻辑
    emit('save', props.patient);
  } catch (error) {
    console.error('保存报告失败:', error);
    ElMessage.error('保存报告失败，请重试');
    ElLoading.service().close();
  }
};

// 组件挂载时获取最新报告
onMounted(() => {
  fetchLatestReport();
});

// 打开图像查看器
const openImageViewer = (imageUrl) => {
  if (!imageUrl) return;
  
  // 先预加载图像以确保图像加载完成
  const img = new Image();
  img.onload = () => {
    viewerImage.value = imageUrl;
    showImageViewer.value = true;
    // 添加body类以防止滚动
    document.body.classList.add('overflow-hidden');
  };
  img.src = imageUrl;
};

// 关闭图像查看器
const closeImageViewer = () => {
  showImageViewer.value = false;
  // 移除body类以允许滚动
  document.body.classList.remove('overflow-hidden');
};

// 格式化显示值，去除方括号等特殊字符
const formatDisplayValue = (value) => {
  if (!value) return null;
  
  // 如果是空数组的字符串表示，返回null
  if (value === '[]' || value === "['']" || value === '[""]') {
    return null;
  }
  
  // 尝试移除方括号和引号
  let formattedValue = value;
  
  // 移除开头的 [' 或 [" 和结尾的 '] 或 "]
  formattedValue = formattedValue.replace(/^\['|^\["|'\]|"\]$/g, '');
  
  // 移除单独的 [ 和 ]
  formattedValue = formattedValue.replace(/^\[|\]$/g, '');
  
  return formattedValue || null;
};
</script>

<style scoped>
.tongue-analysis-report {
  width: 100%;
  color: #333;
}

/* 历史报告选择器样式 */
.history-report-select {
  width: auto;
}

.history-report-select :deep(.el-input__wrapper) {
  box-shadow: none !important;
  padding: 0;
  background-color: transparent;
  border: none;
}

.history-report-select :deep(.el-input__inner) {
  color: #1890ff;
  font-size: 0.875rem;
  background-color: transparent;
  padding: 0 16px 0 0; /* 右侧留出空间给箭头 */
  border: none;
}

.history-report-select :deep(.el-select__caret) {
  color: #1890ff;
  right: 0; /* 将箭头跟文字贴得更近 */
  margin-right: 0;
  font-size: 12px;
  height: 16px;
  line-height: 16px;
  border: none;
}

/* 增加下拉列表的样式 */
.history-report-select :deep(.el-select-dropdown__item) {
  padding: 0 10px;
  height: 30px;
  line-height: 30px;
}

.history-report-select :deep(.el-select-dropdown) {
  margin-top: 5px;
}

/* 当鼠标悬浮时的样式 */
.history-report-select :deep(.el-input__inner:hover) {
  text-decoration: underline;
}

/* 图像查看器样式 */
.image-viewer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.85);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-viewer-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.viewer-img {
  max-width: 80vw; /* u56fau5b9au5bbdu5ea6 */
  max-height: 80vh; /* u56fau5b9au9ad8u5ea6 */
  object-fit: contain;
  display: block;
  margin: 0 auto;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.close-button {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 30px;
  cursor: pointer;
}

@media print {
  .tongue-analysis-report {
    padding: 0;
  }
  
  button {
    display: none;
  }
  
  .image-viewer {
    display: none;
  }
}

/* u6dfbu52a0u56feu50cfu5bb9u5668u7684u4e00u81f4u5c3au5bf8 */
.tongue-image-container {
  width: 160px;
  height: 160px;
  margin: 0 auto;
  transition: all 0.2s ease;
}

.tongue-image-container:hover {
  box-shadow: 0 0 8px rgba(24, 144, 255, 0.2);
  border-color: #1890ff;
}

/* u66f4u7cbe u786eu7684u9009u62e9u5668u6837u5f0f */
.history-report-select {
  width: auto !important;
  display: inline-block;
}

.history-report-select :deep(.el-input__suffix) {
  right: 0;
}
</style>