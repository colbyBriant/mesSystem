<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    width="800px"
    :close-on-click-modal="false"
    @close="closeCamera"
  >
    <div class="video-container bg-white p-4 rounded-lg">
      <!-- 修改摄像头选择下拉框样式 -->
      <div class="mb-4 flex justify-between">
        <div>
          <!-- 添加上传按钮 -->
          <el-upload
            class="upload-button"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            accept="image/*"
            :on-change="handleFileChange"
          >
            <el-button size="small" type="primary">
              本地上传
            </el-button>
          </el-upload>
        </div>
        <el-select
          v-model="selectedCamera"
          placeholder="请选择摄像头"
          @change="handleCameraChange"
          class="w-40"
          size="small"
        >
          <el-option
            v-for="camera in cameras"
            :key="camera.deviceId"
            :label="camera.label"
            :value="camera.deviceId"
          />
        </el-select>
      </div>

      <div class="relative w-full h-[400px] bg-white">
        <video
          v-show="showVideo && !isImageUploaded"
          ref="videoRef"
          class="w-full h-full object-contain"
          autoplay
          playsinline
        ></video>
        <canvas
          v-show="!showVideo || isImageUploaded"
          ref="canvasRef"
          class="w-full h-full object-contain"
        ></canvas>
      </div>
      
      <div class="flex justify-center mt-4 space-x-4">
        <el-button @click="closeCamera" icon="Close">
          关闭
        </el-button>
        <el-button 
          v-if="showVideo && !isImageUploaded" 
          type="primary" 
          @click="capturePhoto"
          icon="Camera"
        >
          拍照
        </el-button>
        <template v-else>
          <el-button @click="retakePhoto" icon="RefreshLeft">
            重拍
          </el-button>
          <el-button type="primary" @click="saveAndAnalyze" icon="Check">
            确定
          </el-button>
        </template>
      </div>
    </div>
    
    <!-- 中医体质辨识问卷弹窗 -->
    <ConstitutionQuestionnaire
      :visible="showQuestionnaire"
      :patientId="patientId"
      @close="handleQuestionnaireClose"
      @submit="handleQuestionnaireSubmit"
    />
  </el-dialog>
</template>

<script setup>
  import { ref, watch, computed } from 'vue';
  import { Camera, RefreshLeft, Check, Close, DataAnalysis } from "@element-plus/icons-vue";
  import { ElMessage, ElLoading } from "element-plus";
  import { saveDiagnosisImage } from '@/api/diagnosis';
  import ConstitutionQuestionnaire from '@/components/ConstitutionQuestionnaire.vue';
  import request from '@/utils/request';
  
  const props = defineProps({
    modelValue: {
      type: Boolean,
      default: false
    },
    cameraType: {
      type: String,
      default: ''
    },
    showVideoValue: {
      type: Boolean,
      default: true
    },
    capturedImageValue: {
      type: String,
      default: ''
    },
    patientId: {  // 新增 patientId prop
      type: String,
      required: true
    }
  });
  
  const emit = defineEmits(['update:modelValue', 'update:showVideoValue', 'update:capturedImageValue', 'close', 'capture', 'retake', 'save', 'analyze']);
  
  // 计算属性：对话框标题
  const title = computed(() => {
    return props.cameraType ? `${props.cameraType}采集` : '图像采集';
  });
  
  const dialogVisible = ref(props.modelValue);
  const showVideo = ref(props.showVideoValue);
  const capturedImage = ref(props.capturedImageValue);
  const videoRef = ref(null);
  const canvasRef = ref(null);
  const stream = ref(null);
  
  // 添加问卷相关状态
  const showQuestionnaire = ref(false);
  const analyzeResult = ref(null);
  
  // 添加上传图片相关状态
  const isImageUploaded = ref(false);
  
  watch(() => props.modelValue, (newVal) => {
    dialogVisible.value = newVal;
  });
  
  watch(dialogVisible, (newVal) => {
    emit('update:modelValue', newVal);
  });
  
  watch(() => props.showVideoValue, (newVal) => {
    showVideo.value = newVal;
  });
  
  watch(showVideo, (newVal) => {
    emit('update:showVideoValue', newVal);
  });
  
  watch(() => props.capturedImageValue, (newVal) => {
    capturedImage.value = newVal;
  });
  
  watch(capturedImage, (newVal) => {
    emit('update:capturedImageValue', newVal);
  });
  
  // 添加打开摄像头的方法
  // 添加摄像头列表和选中摄像头的响应式变量
  const cameras = ref([]);
  const selectedCamera = ref('');
  
  // 获取可用摄像头列表
  const getCameraDevices = async () => {
    try {
      const devices = await navigator.mediaDevices.enumerateDevices();
      cameras.value = devices
        .filter(device => device.kind === 'videoinput')
        .map(device => ({
          deviceId: device.deviceId,
          label: device.label || `摄像头 ${cameras.value.length + 1}`
        }));
      
      // 如果有可用摄像头，默认选择第一个
      if (cameras.value.length > 0 && !selectedCamera.value) {
        selectedCamera.value = cameras.value[0].deviceId;
      }
    } catch (error) {
      console.error('获取摄像头列表失败:', error);
      ElMessage.error('无法获取摄像头列表');
    }
  };
  
  // 修改 initCamera 方法，添加设备 ID 支持
  const initCamera = async () => {
    try {
      stream.value = await navigator.mediaDevices.getUserMedia({
        video: {
          deviceId: selectedCamera.value ? { exact: selectedCamera.value } : undefined,
          width: { ideal: 1280 },
          height: { ideal: 720 }
        }
      });
      if (videoRef.value) {
        videoRef.value.srcObject = stream.value;
      }
    } catch (error) {
      console.error('摄像头初始化失败:', error);
      ElMessage.error('无法访问摄像头，请检查权限设置');
    }
  };
  
  // 处理摄像头切换
  const handleCameraChange = async () => {
    if (stream.value) {
      stream.value.getTracks().forEach(track => track.stop());
    }
    await initCamera();
  };
  
  // 修改对话框打开的监听器
  watch(dialogVisible, async (newVal) => {
    emit('update:modelValue', newVal);
    if (newVal) {
      showVideo.value = true;
      isImageUploaded.value = false;
      await getCameraDevices(); // 获取摄像头列表
      await initCamera();
    } else {
      closeCamera();
    }
  });
  
  // 修改关闭摄像头方法
  const closeCamera = () => {
    if (stream.value) {
      stream.value.getTracks().forEach(track => track.stop());
      stream.value = null;
    }
    if (videoRef.value) {
      videoRef.value.srcObject = null;
    }
    showQuestionnaire.value = false; // 确保问卷关闭
    isImageUploaded.value = false; // 重置上传状态
    emit('close');
  };
  
  // 拍照
  const capturePhoto = () => {
    if (!canvasRef.value || !videoRef.value) return;
    
    const canvas = canvasRef.value;
    const video = videoRef.value;
    
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    
    const context = canvas.getContext("2d");
    context.drawImage(video, 0, 0, canvas.width, canvas.height);
    
    capturedImage.value = canvas.toDataURL("image/png");
    showVideo.value = false;
    
    emit('capture');
  };
  
  // 重新拍照
  const retakePhoto = () => {
    showVideo.value = true;
    capturedImage.value = "";
    isImageUploaded.value = false;
    emit('retake');
  };
  
  // 处理文件上传
  const handleFileChange = (file) => {
    if (!file) return;
    
    // 检查文件类型
    const isImage = file.raw.type.startsWith('image/');
    if (!isImage) {
      ElMessage.error('请上传图片文件!');
      return;
    }
    
    // 读取上传的图片
    const reader = new FileReader();
    reader.onload = (e) => {
      // 获取图片数据
      const imgData = e.target.result;
      
      // 在canvas上显示图片
      const canvas = canvasRef.value;
      const img = new Image();
      img.onload = () => {
        // 设置canvas尺寸与图片一致
        canvas.width = img.width;
        canvas.height = img.height;
        
        // 绘制图片到canvas
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0);
        
        // 保存图片数据
        capturedImage.value = canvas.toDataURL('image/png');
        
        // 更新状态
        showVideo.value = false;
        isImageUploaded.value = true;
        
        console.log('图片已上传并处理');
      };
      img.src = imgData;
    };
    
    // 读取文件为DataURL
    reader.readAsDataURL(file.raw);
  };
  
  // 添加保存并识别方法
  const saveAndAnalyze = async () => {
    try {
      if (!props.patientId) {
        ElMessage.warning('请先选择患者');
        return;
      }
      
      // 显示加载提示
      const loading = ElLoading.service({
        lock: true,
        text: '正在处理图像...',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      
      // 保存图像数据
      const imageData = capturedImage.value;
      
      // 关闭加载提示
      loading.close();
      
      // 如果是舌象分析，先显示问卷弹窗
      if (props.cameraType === '舌象分析') {
        showQuestionnaire.value = true;
      } else {
        // 不是舌象分析，直接发送分析请求
        await sendAnalysisRequest(imageData);
      }
      
    } catch (error) {
      ElLoading.service().close();
      console.error('处理图像失败:', error);
      ElMessage.error(`处理图像失败: ${error.message}`);
    }
  };
  
  // 处理问卷关闭事件
  const handleQuestionnaireClose = () => {
    showQuestionnaire.value = false;
    // 问卷被关闭但没有提交时，不需要自动发送分析请求
  };
  
  // 处理问卷提交事件
  const handleQuestionnaireSubmit = async (questionnaireResult) => {
    showQuestionnaire.value = false;
    
    // 发送图像和问卷一起进行分析
    await sendAnalysisRequest(capturedImage.value, questionnaireResult);
  };
  
  // 发送分析请求
  const sendAnalysisRequest = async (imageData, questionnaireData = null) => {
    try {
      console.log('开始执行sendAnalysisRequest方法...');
      
      // 显示加载提示
      const loading = ElLoading.service({
        lock: true,
        text: '正在分析中...',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      
      // 准备请求数据
      const requestData = {
        imageData: imageData,
        patientId: props.patientId,
        reportType: props.cameraType === '舌象分析' ? 'tongue' : 'face'
      };
      
      // 如果有问卷数据，添加到请求中
      if (questionnaireData) {
        console.log('包含问卷数据', Object.keys(questionnaireData));
        requestData.questionnaire = questionnaireData;
      } else {
        console.log('不包含问卷数据');
      }
      
      console.log('发送分析请求数据:', JSON.stringify({
        patientId: props.patientId,
        reportType: props.cameraType === '舌象分析' ? 'tongue' : 'face',
        hasQuestionnaire: !!questionnaireData,
        imageLength: imageData ? imageData.length : 0
      }));
      
      // 调用后端 prediction 接口，使用request实例
      console.log('即将发送API请求...');
      const result = await request({
        url: '/prediction',
        method: 'POST',
        data: requestData
      });
      
      // 关闭加载提示
      loading.close();
      
      // 完成分析流程
      finishAnalysis(result);
      
    } catch (error) {
      ElLoading.service().close();
      console.error('分析失败:', error);
      ElMessage.error(`分析失败: ${error.response?.data?.message || error.message || '未知错误'}`);
    }
  };
  
  // 完成分析流程
  const finishAnalysis = (result) => {
    // 发送保存事件和分析结果
    emit('analyze', result, capturedImage.value);
    
    // 关闭对话框
    dialogVisible.value = false;
    emit('update:modelValue', false);
    
    // 显示成功消息，并提示用户可以点击"分析报告"按钮查看详细结果
    ElMessage({
      message: '图像分析完成，您可以点击"分析报告"按钮查看详细结果',
      type: 'success',
      duration: 5000
    });
  };
</script>

<style scoped>
.video-container {
  border: 2px solid #eee;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.video-container video,
.video-container canvas {
  background-color: white;
}

/* 添加下拉框样式 */
:deep(.el-select) {
  .el-input__wrapper {
    background-color: #f5f7fa;
    padding: 0 8px;
  }
  .el-input__inner {
    font-size: 13px;
  }
}

/* 上传按钮样式 */
.upload-button {
  display: inline-block;
}
</style>