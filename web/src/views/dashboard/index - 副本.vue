<!-- The exported code uses Tailwind CSS. Install Tailwind CSS in your dev environment to ensure all styles work. -->
<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 顶部导航 -->
    <div class="bg-blue-500 text-white h-14 flex items-center px-4">
      <div class="text-lg font-medium">中医体制辨识系统 v1.0.0</div>
      <div class="flex items-center ml-auto space-x-6">
        <!-- 原有的按钮 -->
        <button
          class="cursor-pointer hover:bg-blue-600 px-4 py-2 rounded-button whitespace-nowrap"
        >
          开始
        </button>
        <button
          class="cursor-pointer hover:bg-blue-600 px-4 py-2 rounded-button whitespace-nowrap"
        >
          查询优先集
        </button>
        <button
          class="cursor-pointer hover:bg-blue-600 px-4 py-2 rounded-button whitespace-nowrap"
        >
          统计分析
        </button>
        <button
          class="cursor-pointer hover:bg-blue-600 px-4 py-2 rounded-button whitespace-nowrap"
        >
          工具
        </button>
        <!-- 添加用户信息和登出按钮 -->
        <div class="flex items-center space-x-4">
          <span>{{ userStore.userInfo?.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>
    
    <div class="flex">
      <!-- 左侧患者列表 -->
      <div
        class="w-64 border-r bg-white h-[calc(100vh-3.5rem)] overflow-y-auto"
      >
        <!-- 修改患者列表标题部分 -->
        <div class="p-4">
          <div class="flex justify-between items-center mb-4">
            <h2 class="font-medium flex items-center">
              患者列表
              <el-icon class="ml-1"><QuestionFilled /></el-icon>
            </h2>
            <el-button type="primary" size="small" @click="handleAddClick">
              新增患者
            </el-button>
          </div>
          
          <!-- 添加搜索框 -->
          <div class="mb-4 flex space-x-2">
            <el-input
              v-model="searchQuery"
              placeholder="搜索患者姓名"
              size="small"
              clearable
            />
            <el-button type="primary" size="small" @click="handleSearch">
              搜索
            </el-button>
          </div>

          <!-- 修改患者列表项 -->
          <div class="space-y-2">
            <div
              v-for="patient in patients"
              :key="patient.id"
              class="p-2 hover:bg-gray-100 rounded cursor-pointer relative group"
              :class="{'bg-blue-50': patient.active}"
            >
              <div @click="selectPatient(patient)">
                <div class="font-medium">{{ patient.name }}</div>
                <div class="text-sm text-gray-500">{{ patient.medicalNumber }}</div>
                <div class="text-xs text-gray-400">
                  身份证：{{ patient.idCard }}
                </div>
              </div>
              <!-- 添加操作按钮 -->
              <div class="absolute right-2 top-2 hidden group-hover:flex space-x-2">
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="handleEditPatient(patient)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  link
                  @click="handleDeletePatient(patient)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 主内容区域 -->
      <div class="flex-1 p-6">
        
        <div class="flex space-x-4 mb-6">
          <button
            class="bg-blue-500 text-white px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
          >
            导入体质
          </button>
          <button
            class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
          >
            手机报告
          </button>
          <button
            class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
          >
            同步上传
          </button>
          <button
            class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
          >
            打印
          </button>
        </div>
        <h2 class="text-xl font-medium mb-6">中医客观化采集</h2>
        <div class="grid grid-cols-3 gap-6 mb-8">
          <div
            v-for="(item, index) in collectionItems"
            :key="index"
            class="bg-white p-6 rounded-lg shadow-sm text-center"
          >
            <img
              :src="item.image"
              :alt="item.title"
              class="w-24 h-24 mx-auto mb-4 rounded-lg object-cover"
            />
            <h3 class="text-lg font-medium mb-3">{{ item.title }}</h3>
            <button
              class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
              @click="handleOpenCamera(item.buttonText)"
            >
              {{ item.buttonText }}
            </button>
          </div>
        </div>
        <!-- 添加相机对话框组件 -->
        <CameraDialog
          v-model="showCamera"
          :camera-type="captureType"
          :patient-id="currentPatient?.id"
          @save="handleCapture"
          @capture="handleCapture"
          @close="showCamera = false"
        />
        <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
          <h3 class="font-medium mb-4 flex items-center">
            <el-icon class="mr-2"><User /></el-icon>
            中医体质辨识
          </h3>
          <div class="space-x-4">
            <button
              class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
            >
              专科人体部位识别
            </button>
            <button
              class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
            >
              九种体质辨识
            </button>
          </div>
        </div>
        <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
          <h3 class="font-medium mb-4 flex items-center">
            <el-icon class="mr-2"><FirstAidKit /></el-icon>
            中医健病管理
          </h3>
          <button
            class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
          >
            高血压辨证
          </button>
        </div>
        <div class="bg-white p-6 rounded-lg shadow-sm">
          <h3 class="font-medium mb-4 flex items-center">
            <el-icon class="mr-2"><Female /></el-icon>
            中医妇幼保健
          </h3>
        </div>
      </div>
      <!-- 右侧信息栏 -->
      <div class="w-64 border-l bg-white p-4">
        <div class="flex items-center justify-center mb-6">
          <el-avatar
            :size="64"
            :src="avatarImage"
          />
        </div>
        <!-- 修改右侧信息栏部分 -->
        <div class="space-y-4 mb-6">
          <div class="text-center">
            <div class="font-medium">{{ currentPatient?.name || '未选择患者' }}</div>
            <div class="text-sm text-gray-500" v-if="currentPatient">
              {{ currentPatient.gender }}, {{ currentPatient.age }}岁
            </div>
          </div>
          <div class="space-y-2" v-if="currentPatient">
            <div class="text-sm">
              <span class="text-gray-500">联系电话：</span>
              <span>{{ currentPatient.phone }}</span>
            </div>
            <div class="text-sm">
              <span class="text-gray-500">居住地址：</span>
              <span>{{ currentPatient.address }}</span>
            </div>
            <div class="text-sm">
              <span class="text-gray-500">主治医生：</span>
              <span>{{ currentPatient.doctor }}</span>
            </div>
            <div class="text-sm">
              <span class="text-gray-500">就诊科室：</span>
              <span>{{ currentPatient.department }}</span>
            </div>
            <div class="text-sm">
              <span class="text-gray-500">检测编号：</span>
              <span>{{ currentPatient.testNumber }}</span>
            </div>
            <div class="text-sm">
              <span class="text-gray-500">检测时间：</span>
              <span>{{ currentPatient.testTime }}</span>
            </div>
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div
            v-for="(action, index) in quickActions"
            :key="index"
            class="p-3 text-center rounded cursor-pointer"
            :class="action.bgColor"
          >
            <el-icon class="text-2xl mb-1" :class="action.iconColor">
              <component :is="action.icon" />
            </el-icon>
            <div class="text-sm">{{ action.title }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <el-dialog
  v-model="dialogVisible"
  :title="isEditing ? '编辑患者' : '新建采集'"
  width="500px"
  :close-on-click-modal="false"
>
  <el-tabs v-model="activeTab">
    <el-tab-pane label="首次检测" name="first">
      <el-form :model="newPatientForm" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="newPatientForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" required>
          <el-select v-model="newPatientForm.gender" placeholder="请选择性别" class="w-full">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="身份证号" required>
          <el-input 
            v-model="newPatientForm.idCard" 
            placeholder="请输入身份证号"
            @change="handleIdCardChange"
          />
        </el-form-item>
        <el-form-item label="就诊号">
          <el-input v-model="newPatientForm.medicalNumber" placeholder="系统自动生成" disabled />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="newPatientForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="居住地址">
          <el-input v-model="newPatientForm.address" placeholder="请输入居住地址" />
        </el-form-item>
        <el-form-item label="就诊卡号">
          <el-input v-model="newPatientForm.cardNumber" placeholder="请输入就诊卡号" />
        </el-form-item>
      </el-form>
    </el-tab-pane>
    <el-tab-pane label="再次检测" name="second">
      <div class="flex items-center justify-center h-64 text-gray-500">
        <el-empty description="此功能正在开发中...">
          <template #image>
            <el-icon class="text-6xl text-gray-300"><Tools /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-tab-pane>
    <el-tab-pane label="批量新建" name="batch">
      <div class="flex items-center justify-center h-64 text-gray-500">
        <el-empty description="此功能正在开发中...">
          <template #image>
            <el-icon class="text-6xl text-gray-300"><Tools /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-tab-pane>
  </el-tabs>
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleAddPatient">确定</el-button>
    </span>
  </template>
</el-dialog>
</template>

<script setup>
import { ref, markRaw, onMounted } from "vue";
import CameraDialog from '@/components/dashboard/CameraDialog.vue';  // 添加导入
import { ElMessage, ElMessageBox } from 'element-plus';  // 添加 ElMessageBox 导入
import { useUserStore } from '@/stores/user'  
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import {
  User, Female, QuestionFilled, Document,
  DataLine, More, Search, FirstAidKit,
  Tools  // 添加 Tools 图标
} from "@element-plus/icons-vue";

const router = useRouter();
const userStore = useUserStore();
const patients = ref([]);
const currentPatient = ref(null);

  // 添加新增患者相关的响应式变量
  const dialogVisible = ref(false);
const activeTab = ref('first');
const newPatientForm = ref({
  name: '',
  gender: '',
  idCard: '',
  medicalNumber: '',
  phone: '',
  address: '',
  cardNumber: ''
});

// 添加搜索相关的变量
const searchQuery = ref('');

// 添加搜索方法
const handleSearch = async () => {
  try {
    const response = await request.get('/patients/search', {
      params: { name: searchQuery.value }
    });
    const patientData = Array.isArray(response) ? response : [response];
    patients.value = patientData.map(patient => ({
      ...patient,
      active: false
    }));
  } catch (error) {
    ElMessage.error('搜索失败');
    console.error(error);
  }
};

// 添加编辑患者方法
const handleEditPatient = (patient) => {
  newPatientForm.value = {
    name: patient.name,
    gender: patient.gender,
    idCard: patient.idCard,
    medicalNumber: patient.medicalNumber,
    phone: patient.phone || '',
    address: patient.address || '',
    cardNumber: patient.cardNumber || ''
  };
  dialogVisible.value = true;
  isEditing.value = true;
  editingPatientId.value = patient.id;
};
// 添加登出方法
const handleLogout = async () => {
  try {
    await request.post('/users/logout');
    userStore.logout();
    ElMessage.success('退出成功');
    router.push('/login');
  } catch (error) {
    console.error('登出失败:', error);
    ElMessage.error('退出失败');
  }
};
// 添加删除患者方法
const handleDeletePatient = async (patient) => {
  try {
    // 确保用户点击了确认按钮
    const confirmResult = await ElMessageBox.confirm('确定要删除该患者吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    // 只有当用户点击确认时才执行删除操作
    if (confirmResult === 'confirm') {
      console.log('删除患者:', patient.id); // 添加调试日志
      await request.delete(`/patients/${patient.id}`);
      ElMessage.success('删除成功');
      fetchPatients();
    }
  } catch (error) {
    // 区分取消操作和真正的错误
    if (error !== 'cancel') {
      console.error('删除失败:', error); // 添加详细错误信息
      ElMessage.error(`删除失败: ${error.response?.data?.message || '未知错误'}`);
    }
  }
};

// 修改现有的 handleAddPatient 方法
const handleAddPatient = async () => {
  try {
    if (!newPatientForm.value.name || !newPatientForm.value.gender || !newPatientForm.value.idCard) {
      ElMessage.warning('请填写必填项');
      return;
    }
    
    const patientData = {
      name: newPatientForm.value.name,
      gender: newPatientForm.value.gender,
      idCard: newPatientForm.value.idCard,
      medicalNumber: isEditing.value ? newPatientForm.value.medicalNumber : generateMedicalNumber(),
      phone: newPatientForm.value.phone,
      address: newPatientForm.value.address,
      cardNumber: newPatientForm.value.cardNumber
    };
    
    if (isEditing.value) {
      await request.put(`/patients/${editingPatientId.value}`, patientData);
      ElMessage.success('修改患者成功');
    } else {
      await request.post('/patients', patientData);
      ElMessage.success('添加患者成功');
    }
    
    dialogVisible.value = false;
    isEditing.value = false;
    editingPatientId.value = null;
    
    // 重置表单
    newPatientForm.value = {
      name: '',
      gender: '',
      birthDate: '',
      number: '',
      unit: ''
    };
    
    // 刷新患者列表
    fetchPatients();
  } catch (error) {
    ElMessage.error(isEditing.value ? '修改患者失败' : '添加患者失败');
    console.error(error);
  }
};

// 添加编辑相关的状态变量
const isEditing = ref(false);
const editingPatientId = ref(null);
const calculateAge = (birthDate) => {
  const today = new Date();
  const birth = new Date(birthDate);
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--;
  }
  
  return age;
};

// 检查登录状态
const checkAuth = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return false;
  }
  return true;
};

// 获取患者列表
const fetchPatients = async () => {
  try {
    const response = await request.get('/patients');
    // 检查响应数据结构并做相应处理
    const patientData = Array.isArray(response) ? response : [response];
    patients.value = patientData.map(patient => ({
      ...patient,
      active: false
    }));
    if (patients.value.length > 0) {
      selectPatient(patients.value[0]);
    }
  } catch (error) {
    ElMessage.error('获取患者列表失败');
    console.error(error);
  }
};

const selectPatient = (patient) => {
  
  // 取消所有患者的高亮状态
  patients.value.forEach(p => p.active = false);
  
  // 设置当前选中患者的高亮状态
  if (patient) {
    patient.active = true;
    currentPatient.value = patient;
  } else {
    currentPatient.value = null;
  }
  
  const processedPatient = {
    ...patient,
    age: patient.age ?? '未知',
    phone: patient.phone ?? '未填写',
    address: patient.address ?? '未填写',
    cardNumber: patient.cardNumber ?? '未填写'
  };

  patients.value.forEach(p => p.active = false);
  processedPatient.active = true;
  currentPatient.value = processedPatient;
};


// 导入图片
const collectionItems = ref([
  {
    title: "面色采集",
    buttonText: "面色分析",
    image: "/src/assets/images/face.jpg",
  },
  {
    title: "舌象采集",
    buttonText: "舌象分析",
    image: "/src/assets/images/tongue.jpg",
  },
  {
    title: "脉象采集",
    buttonText: "脉象回放",
    image: "/src/assets/images/pulse.jpg",
  },
]);

const avatarImage = "/src/assets/images/avatar.jpg";

const quickActions = ref([
  {
    title: "医生手册",
    icon: markRaw(Document),
    bgColor: "bg-blue-100",
    iconColor: "text-blue-500",
  },
  {
    title: "数据统计",
    icon: markRaw(DataLine),
    bgColor: "bg-green-100",
    iconColor: "text-green-500",
  },
  {
    title: "更多功能",
    icon: markRaw(More),
    bgColor: "bg-purple-100",
    iconColor: "text-purple-500",
  },
  {
    title: "信息查询",
    icon: markRaw(Search),
    bgColor: "bg-orange-100",
    iconColor: "text-orange-500",
  },
]);

//控制摄像头弹窗
const showCamera = ref(false);
const captureType = ref('');

// 处理打开相机
const handleOpenCamera = (type) => {
  if (!currentPatient.value) {
    ElMessage.warning('请先选择患者');
    return;
  }
  captureType.value = type;
  showCamera.value = true;
};
// 添加 handleAddClick 方法
const handleAddClick = () => {
  // 重置表单数据
  newPatientForm.value = {
    name: '',
    gender: '',
    idCard: '',
    medicalNumber: '',
    phone: '',
    address: '',
    cardNumber: ''
  };
  // 重置编辑状态
  isEditing.value = false;
  editingPatientId.value = null;
  activeTab.value = 'first';  // 重置标签页
  dialogVisible.value = true;  // 打开对话框
};

// 添加生成就诊号的方法
const generateMedicalNumber = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
  return `MN${year}${month}${day}${random}`;
};

// 修改处理照片捕获方法
const handleCapture = async (imageData) => {
  try {
    if (!currentPatient.value?.id) {
      ElMessage.warning('请先选择患者');
      return;
    }

    const data = {
      patientId: currentPatient.value.id,
      tongueImage: captureType.value === '舌象分析' ? imageData : '',
      faceImage: captureType.value === '面色分析' ? imageData : '',
      tongueAnalysis: captureType.value === '舌象分析' ? '舌象分析结果' : '',
      faceAnalysis: captureType.value === '面色分析' ? '面象分析结果' : ''
    };
    
    const response = await request.post(`/patients/${currentPatient.value.id}/images`, data);
    
    // 检查是否上传成功
    if (response.id) {
      
      // 获取更新后的图片数据
      const imagesResponse = await request.get(`/patients/${currentPatient.value.id}/images`);
      
      // 处理返回的图片数据
      imagesResponse.forEach(image => {
        if (image.type === 'tongue') {
          patientImages.value.tongue = image;
        } else if (image.type === 'face') {
          patientImages.value.face = image;
        }
      });
    } else {
      throw new Error(`上传失败：${response.message || '未知错误'}`);
    }
  } catch (error) {
    console.error('采集失败:', error);
    
    let errorMessage = `${captureType.value}失败`;
    if (error.response) {
      if (error.response.status === 401) {
        errorMessage = '请先登录';
      } else if (error.response.status === 404) {
        errorMessage = '患者不存在';
      } else if (error.response.status === 413) {
        errorMessage = '图片文件过大';
      } else if (error.response.data?.message) {
        errorMessage = error.response.data.message;
      }
    }
    ElMessage.error(errorMessage);
  }
};

// 添加用于存储患者图片的响应式变量
const patientImages = ref({
  tongue: null,
  face: null
});

// 组件挂载时检查登录状态并获取数据
onMounted(() => {
  if (checkAuth()) {
    fetchPatients();
  }
});
</script>

<style scoped>
.patient-list::-webkit-scrollbar {
  width: 6px;
}
.patient-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}
.patient-list::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}
.patient-list::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>