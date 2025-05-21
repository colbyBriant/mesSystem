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
          @click="showDevelopingMessage"
        >
          开始
        </button>
        <button
          class="cursor-pointer hover:bg-blue-600 px-4 py-2 rounded-button whitespace-nowrap"
          @click="showDevelopingMessage"
        >
          查询优先集
        </button>
        <button
          class="cursor-pointer hover:bg-blue-600 px-4 py-2 rounded-button whitespace-nowrap"
          @click="showDevelopingMessage"
        >
          统计分析
        </button>
        <button
          class="cursor-pointer hover:bg-blue-600 px-4 py-2 rounded-button whitespace-nowrap"
          @click="showDevelopingMessage"
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
        class="w-64 border-r bg-white h-[calc(100vh-3.5rem)] overflow-y-auto hover:overflow-y-scroll"
        style="scrollbar-width: thin;"
      >
        <!-- 修改患者列表标题部分 -->
        <div class="p-4">
          <div class="flex justify-between items-center mb-4">
            <h2 class="font-medium flex items-center">
              采集列表
              <el-icon class="ml-1"><QuestionFilled /></el-icon>
            </h2>
            <el-button type="primary" size="small" @click="handleAddClick">
              新建采集
            </el-button>
          </div>
          
          <!-- 修改搜索框部分 -->
          <div class="mb-4">
            <el-input
              v-model="searchQuery"
              placeholder="搜索患者姓名"
              size="big"
              clearable
              @input="debouncedSearch"
            />
          </div>

          <!-- 修改患者列表项 -->
          <div class="space-y-2">
            <div
              v-for="patient in patients"
              :key="patient.id"
              class="p-2 rounded cursor-pointer relative group transition-all duration-200"
              :class="{
                'bg-gradient-to-r from-blue-500/10 to-indigo-500/10 border border-blue-100 shadow-lg': patient.active,
                'hover:bg-gray-50/80 border border-transparent': !patient.active
              }"
              @click="selectPatient(patient)"
            >
              <div>
                <div class="font-medium" :class="{'text-blue-700': patient.active}">{{ patient.name }}</div>
                <div class="text-sm" :class="{
                  'text-blue-600': patient.active,
                  'text-gray-500': !patient.active
                }">
                  {{ patient.medicalNumber }}
                </div>
                <div class="text-xs" :class="{
                  'text-blue-500': patient.active,
                  'text-gray-400': !patient.active
                }">
                  身份证：{{ patient.idCard }}
                </div>
              </div>
              <!-- 修改操作按钮样式 -->
              <div class="absolute right-2 top-2 space-x-2" :class="{'flex': patient.active, 'hidden': !patient.active}">
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click.stop="handleEditPatient(patient)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  link
                  @click.stop="handleDeletePatient(patient)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
          <!-- 确保分页栏容器可见 -->
          <div class="sticky bottom-0 bg-white border-t p-2 shadow-sm" style="z-index: 1;">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="totalPatients"
              :page-sizes="[10, 20, 50]"
              layout="prev, pager, next"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
              class="pagination-custom"
              background
              :pager-count="5"
              small
            />
          </div>
      </div>
      <!-- 主内容区域 -->
      <div class="flex-1 p-6">
        <div class="flex justify-between mb-6">
          <!-- 左侧按钮组 -->
          <div class="flex space-x-4">
            <!-- 添加主页按钮 -- 默认激活 -->
            <button
              class="px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
              :class="!showReport ? 'bg-blue-500 text-white' : 'border border-gray-300'"
              @click="showReport = false"
            >
              主页
            </button>
            <!-- 分析报告按钮 -->
            <button
              v-if="hasAnalysisData"
              class="px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
              :class="showReport ? 'bg-blue-500 text-white' : 'border border-gray-300'"
              @click="showAnalysisReport"
            >
              分析报告
            </button>
          </div>
          <!-- 右侧按钮组 -->
          <div class="flex space-x-4">
            <button
            class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
            @click="handleImportClick"
          >
            导入体质
            </button>
            <button
              class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
              @click="showDevelopingMessage"
            >
              手机报告
            </button>
            <button
              class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
              @click="showDevelopingMessage"
            >
              同步上传
            </button>
            <button
              class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
              @click="showDevelopingMessage"
            >
              打印
            </button>
          </div>
        </div>
        <!-- 根据状态显示不同内容 -->
        <div v-if="!showReport" class="overflow-y-auto scrollbar-hide" style="max-height: calc(100vh - 9rem);">
          <h2 class="text-xl font-medium mb-6">中医客观化采集</h2>
          <div class="grid grid-cols-2 gap-6 mb-8">
            <div
              v-for="(item, index) in collectionItems"
              :key="index"
              class="bg-white p-8 rounded-lg shadow-md text-center flex flex-col items-center justify-between h-80 transform transition-all duration-300 hover:shadow-xl hover:-translate-y-1 w-full"
            >
              <div class="flex-1 flex items-center justify-center w-full">
                <img
                  :src="item.image"
                  :alt="item.title"
                  class="w-40 h-40 rounded-lg object-cover transition-transform duration-300 hover:scale-105"
                />
              </div>
              <div class="mt-4">
                <button
                  class="border border-gray-300 px-6 py-2 rounded-button whitespace-nowrap cursor-pointer hover:bg-blue-50 hover:border-blue-300 transition-colors duration-300"
                  @click="handleOpenCamera(item.buttonText)"
                >
                  {{ item.buttonText }}
                </button>
              </div>
            </div>
          </div>
          <!-- 添加相机对话框组件 -->
          <CameraDialog
            v-model="showCamera"
            :camera-type="captureType"
            :patient-id="currentPatient?.id"
            @save="handlePreview"
            @capture="handlePreview"
            @close="showCamera = false"
            @analyze="handleAnalyzeResult"
          />
          <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
            <h3 class="font-medium mb-4 flex items-center">
              <el-icon class="mr-2"><User /></el-icon>
              中医体质辨识
            </h3>
            <div class="space-x-4">
              <button
                class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
                @click="showDevelopingMessage"
              >
                专科人体部位识别
              </button>
              <button
                class="border border-gray-300 px-4 py-2 rounded-button whitespace-nowrap cursor-pointer"
                @click="showDevelopingMessage"
              >
                九种体质辨识
              </button>
            </div>
          </div>
          <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
            <h3 class="font-medium mb-4 flex items-center">
              <el-icon class="mr-2"><FirstAidKit /></el-icon>
              中医健康管理
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
            <div class="space-y-3">
              <!-- 女性检查方案 -->
              <div class="flex items-center space-x-2">
                <span class="text-gray-600 w-16">女性：</span>
                <button
                  class="border border-gray-300 px-3 py-1.5 rounded-button text-sm hover:bg-gray-50"
                  @click="showDevelopingMessage"
                >
                  备孕
                </button>
                <button
                  class="border border-gray-300 px-3 py-1.5 rounded-button text-sm hover:bg-gray-50"
                  @click="showDevelopingMessage"
                >
                  中医体质辨识
                </button>
              </div>

              <!-- 儿童检查方案 -->
              <div class="flex items-center space-x-2">
                <span class="text-gray-600 w-16">儿童：</span>
                <button
                  class="border border-gray-300 px-3 py-1.5 rounded-button text-sm hover:bg-gray-50"
                  @click="showDevelopingMessage"
                >
                  中医体质辨识
                </button>
                <button
                  class="border border-gray-300 px-3 py-1.5 rounded-button text-sm hover:bg-gray-50"
                  @click="showDevelopingMessage"
                >
                  健康检查记录
                </button>
                <button
                  class="border border-gray-300 px-3 py-1.5 rounded-button text-sm hover:bg-gray-50"
                  @click="showDevelopingMessage"
                >
                  记录报告
                </button>
              </div>

              <!-- 婴幼儿检查方案 -->
              <div class="flex items-center space-x-2">
                <span class="text-gray-600 w-16">婴幼儿：</span>
                <button
                  class="border border-gray-300 px-3 py-1.5 rounded-button text-sm hover:bg-gray-50"
                  @click="showDevelopingMessage"
                >
                  中医体质辨识
                </button>
                <button
                  class="border border-gray-300 px-3 py-1.5 rounded-button text-sm hover:bg-gray-50"
                  @click="showDevelopingMessage"
                >
                  护理建议
                </button>
                <button
                  class="border border-gray-300 px-3 py-1.5 rounded-button text-sm hover:bg-gray-50"
                  @click="showDevelopingMessage"
                >
                  健康检查记录
                </button>
                <button
                  class="border border-gray-300 px-3 py-1.5 rounded-button text-sm hover:bg-gray-50"
                  @click="showDevelopingMessage"
                >
                  记录报告
                </button>
              </div>
            </div>
          </div>
        </div>
        <!-- 分析报告页面 -->
        <div v-else class="overflow-y-auto scrollbar-hide" style="max-height: calc(100vh - 9rem);">
          <TongueAnalysisReport 
            :patient="currentPatient" 
            :report-data="currentReport"
            @close="showReport = false"
            @save="handleSavePatientReport"
          />
        </div>
      </div>
      <!-- 右侧信息栏 -->
      <div class="w-80 border-l bg-white p-6">
        <div class="flex items-center justify-center mb-8">
          <el-avatar
            :size="80"
            :src="avatarImage"
          />
        </div>
        <!-- 修改右侧信息栏部分 -->
        <div class="space-y-6 mb-8">
          <div class="text-center">
            <div class="text-xl font-medium mb-2">{{ currentPatient?.name || '未选择患者' }}</div>
            <div class="text-base text-gray-600" v-if="currentPatient">
              {{ currentPatient.gender }}{{ currentPatient.age ? currentPatient.age + '岁' : '' }}
            </div>
          </div>
          <div class="space-y-4" v-if="currentPatient">
            <div class="text-base">
              <span class="text-gray-600 inline-block w-24">联系电话：</span>
              <span>{{ currentPatient.phone }}</span>
            </div>
            <div class="text-base">
              <span class="text-gray-600 inline-block w-24">居住地址：</span>
              <span>{{ currentPatient.address }}</span>
            </div>
            <div class="text-base">
              <span class="text-gray-600 inline-block w-24">主治医生：</span>
              <span>{{ currentPatient.doctor }}</span>
            </div>
            <div class="text-base">
              <span class="text-gray-600 inline-block w-24">就诊科室：</span>
              <span>{{ currentPatient.department }}</span>
            </div>
            <div class="text-base">
              <span class="text-gray-600 inline-block w-24">检测编号：</span>
              <span>{{ currentPatient.testNumber }}</span>
            </div>
            <div class="text-base">
              <span class="text-gray-600 inline-block w-24">检测时间：</span>
              <span>{{ currentPatient.testTime }}</span>
            </div>
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div
            class="p-3 text-center rounded cursor-pointer bg-blue-100 hover:bg-blue-200 transition-colors duration-300"
            @click="showDevelopingMessage"
          >
            <el-icon class="text-2xl mb-1 text-blue-500">
              <Document />
            </el-icon>
            <div class="text-sm">医生手册</div>
          </div>
          <div
            class="p-3 text-center rounded cursor-pointer bg-green-100 hover:bg-green-200 transition-colors duration-300"
            @click="showDevelopingMessage"
          >
            <el-icon class="text-2xl mb-1 text-green-500">
              <DataLine />
            </el-icon>
            <div class="text-sm">数据统计</div>
          </div>
          <div
            class="p-3 text-center rounded cursor-pointer bg-purple-100 hover:bg-purple-200 transition-colors duration-300"
            @click="showDevelopingMessage"
          >
            <el-icon class="text-2xl mb-1 text-purple-500">
              <More />
            </el-icon>
            <div class="text-sm">更多功能</div>
          </div>
          <div
            class="p-3 text-center rounded cursor-pointer bg-orange-100 hover:bg-orange-200 transition-colors duration-300"
            @click="showDevelopingMessage"
          >
            <el-icon class="text-2xl mb-1 text-orange-500">
              <Search />
            </el-icon>
            <div class="text-sm">信息查询</div>
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
      <el-form :model="newPatientForm" label-width="100px" :rules="formRules" ref="patientFormRef">
        <el-form-item label="姓名" required prop="name">
          <el-input v-model="newPatientForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" required prop="gender">
          <el-select v-model="newPatientForm.gender" placeholder="请选择性别" class="w-full">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="身份证号" required prop="idCard">
          <el-input 
            v-model="newPatientForm.idCard" 
            placeholder="请输入身份证号"
            @change="handleIdCardChange"
          />
        </el-form-item>
        <el-form-item label="就诊号">
          <el-input v-model="newPatientForm.medicalNumber" placeholder="系统自动生成" disabled />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="newPatientForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="居住地址" prop="address">
          <el-input v-model="newPatientForm.address" placeholder="请输入居住地址" />
        </el-form-item>
        <el-form-item label="就诊卡号" prop="cardNumber">
          <el-input v-model="newPatientForm.cardNumber" placeholder="请输入就诊卡号" />
        </el-form-item>
      </el-form>
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
<!-- 添加扫码弹窗 -->
<el-dialog
  v-model="scanDialogVisible"
  title="导入体质辨识"
  width="360px"
  :close-on-click-modal="false"
  :show-close="true"
  class="scan-dialog"
  @close="handleDialogClose"
>
  <div class="p-3">
    <p class="text-center text-sm mb-4 leading-6">
      请将扫描枪手柄扫描二维码或条形码，提示音后即可完成。<br/>
      信息读取成功后请点击确定完成导入。
    </p>
    
    <div class="text-center text-xs text-blue-500 mb-4">
      <a href="#" @click.prevent="showHelpMessage">设备启动中请稍等...</a>
    </div>
    
    <div class="mb-5">
      <div class="flex items-center">
        <span class="text-sm mr-2 whitespace-nowrap">码值：</span>
        <el-input
          v-model="scanCode"
          placeholder=""
          @keyup.enter="handleScanComplete"
          ref="scanInput"
          class="flex-1"
          size="default"
        />
      </div>
    </div>

    <div class="flex justify-center space-x-3">
      <button class="scan-button min-w-[70px]" @click="handleScanComplete">确定</button>
      <button class="scan-button min-w-[70px]" @click="handleClear">清空</button>
      <button class="scan-button min-w-[70px]" @click="handleCancel">取消</button>
    </div>
  </div>
</el-dialog>
</template>

<script setup>
import { ref, markRaw, onMounted, onUnmounted, nextTick, watch, reactive } from "vue";
import CameraDialog from '@/components/CameraDialog.vue';
import TongueAnalysisReport from '@/components/TongueAnalysisReport.vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user'  
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import {
  User, Female, QuestionFilled, Document,
  DataLine, More, Search, FirstAidKit,
  Tools
} from "@element-plus/icons-vue";

const router = useRouter();
const userStore = useUserStore();
const patients = ref([]);
const currentPatient = ref(null);

//控制摄像头弹窗
const showCamera = ref(false);
const captureType = ref('');

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

// 添加表单引用
const patientFormRef = ref(null);

// 身份证号验证函数
const validateIdCard = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入身份证号'));
  }
  
  // 身份证号码格式正则
  const idCardReg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
  
  if (!idCardReg.test(value)) {
    return callback(new Error('身份证号码格式不正确'));
  }
  
  // 18位身份证需要验证最后一位校验位
  if (value.length === 18) {
    const idCardWi = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]; // 加权因子
    const idCardY = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']; // 校验码
    let idCardWiSum = 0; // 用来保存前17位各自乘以加权因子后的总和
    
    for (let i = 0; i < 17; i++) {
      idCardWiSum += parseInt(value.substring(i, i + 1)) * idCardWi[i];
    }
    
    const idCardMod = idCardWiSum % 11; // 计算出校验码所在的位置
    const idCardLast = value.substring(17); // 得到最后一位身份证号码
    
    // 如果等于2，则说明校验码是X，否则为数字
    if (idCardMod === 2) {
      if (idCardLast.toUpperCase() !== 'X') {
        return callback(new Error('身份证号码校验位错误'));
      }
    } else {
      if (idCardLast !== idCardY[idCardMod]) {
        return callback(new Error('身份证号码校验位错误'));
      }
    }
  }
  
  // 验证通过
  callback();
};

// 手机号验证函数
const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    return callback(new Error('请输入正确的手机号码'));
  }
  callback();
};

// 表单验证规则
const formRules = reactive({
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { validator: validateIdCard, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
});

// 添加提示方法
// 扫码相关的响应式变量
const scanDialogVisible = ref(false)
const scanCode = ref('');
const scanBuffer = ref('');
const scanResult = ref(null);
const scanInput = ref(null);
let scanTimeout = null;
let lastInputTime = null;
const SCAN_TIMEOUT = 100; // 扫码超时时间（毫秒）
const MIN_SCAN_LENGTH = 8; // 最小有效扫码长度
const MAX_INPUT_INTERVAL = 50; // 最大输入间隔（毫秒）

// 添加搜索相关的变量
const searchQuery = ref('');

// 添加分页相关变量
const currentPage = ref(1);
const pageSize = ref(10);
const totalPatients = ref(0);
// 处理对话框关闭
const handleDialogClose = () => {
  scanDialogVisible.value = false;
  scanCode.value = '';
  scanBuffer.value = '';
  lastInputTime = null;
  if (scanTimeout) {
    clearTimeout(scanTimeout);
    scanTimeout = null;
  }
}

// 处理取消按钮点击
const handleCancel = () => {
  scanCode.value = ''
  scanDialogVisible.value = false
}

// 处理清空按钮点击
const handleClear = () => {
  scanCode.value = ''
}
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

// 添加用于存储患者图片的响应式变量
const patientImages = ref({
  tongue: null,
  face: null
});

// 添加分析报告相关变量
const hasAnalysisData = ref(true);
const showReport = ref(false);
// 添加分析报告数据状态
const currentReport = ref(null);

// 显示分析报告
const showAnalysisReport = () => {
  // 检查是否有选中的患者
  if (!currentPatient.value) {
    ElMessage.warning('请先选择一个患者');
    return;
  }
  
  // 检查是否有分析报告数据
  if (!hasAnalysisData.value || !currentReport.value) {
    ElMessage.info('该患者暂无分析报告');
    return;
  }
  
  // 有分析报告数据，显示分析报告页面
  showReport.value = true;
};

// 处理保存患者报告
const handleSavePatientReport = async (patient) => {
  try {
    await request.post(`/patients/${patient.id}/reports/save`);
    ElMessage.success('报告保存成功');
  } catch (error) {
    console.error('保存报告失败:', error);
    ElMessage.error('保存报告失败');
  }
};



// 修改键盘事件监听函数
const handleKeyDown = (e) => {
  if (!scanDialogVisible.value) return;
  
  const currentTime = Date.now();
  
  // 检查是否是人为输入（输入间隔过长）
  if (lastInputTime && (currentTime - lastInputTime) > MAX_INPUT_INTERVAL) {
    scanBuffer.value = ''; // 清空缓冲区
    lastInputTime = currentTime;
    return;
  }
  
  // 更新最后输入时间
  lastInputTime = currentTime;
  
  // 清除之前的超时
  if (scanTimeout) {
    clearTimeout(scanTimeout);
  }
  
  // 过滤无效字符，只接受数字、字母和常见特殊字符
  if (/^[0-9a-zA-Z\-_+=.]+$/.test(e.key) && e.key.length === 1) {
    scanBuffer.value += e.key;
  } else if (e.key === 'Enter') {
    // 扫码枪通常以回车结束
    if (scanBuffer.value.length >= MIN_SCAN_LENGTH) {
      scanCode.value = scanBuffer.value;
      scanBuffer.value = '';
      lastInputTime = null;
      handleScanComplete();
    } else {
      // 输入长度不足，可能是人为输入
      scanBuffer.value = '';
    }
    return;
  }
  
  // 设置新的超时
  scanTimeout = setTimeout(() => {
    if (scanBuffer.value) {
      // 超时后检查输入长度
      if (scanBuffer.value.length >= MIN_SCAN_LENGTH) {
        scanCode.value = scanBuffer.value;
        handleScanComplete();
      }
      scanBuffer.value = ''; // 清空缓冲区
      lastInputTime = null;
    }
  }, SCAN_TIMEOUT);
};

// 修改导入体质按钮的点击事件
const handleImportClick = () => {
  scanDialogVisible.value = true;
  // 等待DOM更新后聚焦输入框
  nextTick(() => {
    scanInput.value?.focus();
  });
};

// 处理扫码完成
const handleScanComplete = async () => {
  if (!scanCode.value) return;
  
  try {
    // 调用后端API获取患者信息
    const response = await request.get(`/patients/scan/${scanCode.value}`);
    
    scanResult.value = response.data;
    
    // 更新当前患者信息
    currentPatient.value = response.data;
    
    // 清空扫码输入框
    scanCode.value = '';
    
    // 显示成功提示
    ElMessage.success('患者信息导入成功');
    
    // 关闭弹窗
    setTimeout(() => {
      scanDialogVisible.value = false;
      scanResult.value = null;
    }, 1500);
  } catch (error) {
    console.error('扫码处理错误:', error);
    ElMessage.error('获取患者信息失败，请重试');
  }
};

// 显示帮助信息
const showDevelopingMessage = () => {
  ElMessage({
    message: '此功能尚在开发中...',
    type: 'info',
    duration: 2000
  })
}
// 添加搜索方法
const handleSearch = async () => {
  try {
    // 重置到第一页
    currentPage.value = 1;
    
    // 如果搜索查询为空，则获取所有患者
    if (!searchQuery.value || searchQuery.value.trim() === '') {
      await fetchPatients();
      return;
    }
    
    // 否则执行搜索
    const response = await request.get('/patients/search', {
      params: { 
        name: searchQuery.value,
        page: currentPage.value - 1,  // 后端分页从0开始
        size: pageSize.value
      }
    });
    
    // 处理响应数据
    if (response.content) {
      // 如果是分页响应
      patients.value = response.content.map(patient => ({
        ...patient,
        active: currentPatient.value?.id === patient.id
      }));
      totalPatients.value = response.totalElements || 0;
    } else {
      // 如果是普通数组响应
      const patientData = Array.isArray(response) ? response : [response];
      patients.value = patientData.map(patient => ({
        ...patient,
        active: currentPatient.value?.id === patient.id
      }));
      totalPatients.value = patientData.length;
    }
    
    // 如果搜索结果为空，清空当前选中的患者
    if (patients.value.length === 0) {
      currentPatient.value = null;
    }
  } catch (error) {
    ElMessage.error('搜索失败');
    console.error(error);
  }
};

// 简单的防抖函数
function debounce(fn, delay) {
  let timer = null;
  return function() {
    const context = this;
    const args = arguments;
    clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(context, args);
    }, delay);
  };
}

const debouncedSearch = debounce(handleSearch, 300);

// 修改获取患者列表方法
const loading = ref(false);
const fetchPatients = async (keepSelectedId = null) => {
  try {
    loading.value = true;
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: 'createTime,desc'
    };
    
    if (searchQuery.value) {
      params.name = searchQuery.value;
    }
    
    const response = await request.get('/patients', { params });
    const patientData = response.content || [];
    totalPatients.value = response.totalElements || 0;
    
    // 处理患者数据，标记活跃状态
    patients.value = patientData.map(patient => {
      // 如果有指定要保持选中的ID，则使用它；否则使用当前选中的患者ID
      const selectedId = keepSelectedId || (currentPatient.value ? currentPatient.value.id : null);
      return {
        ...patient,
        age: calculateAge(patient.birthDate),
        active: patient.id === selectedId
      };
    });
    
    // 如果有指定要保持选中的ID，则更新当前患者
    if (keepSelectedId) {
      const selectedPatient = patients.value.find(p => p.id === keepSelectedId);
      if (selectedPatient) {
        currentPatient.value = selectedPatient;
      }
    } else if (patients.value.length > 0 && !currentPatient.value) {
      // 只有在没有当前选中患者时，才默认选中第一个
      selectPatient(patients.value[0]);
    }
  } catch (error) {
    ElMessage.error('获取患者列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 修改分页改变处理方法
const handlePageChange = (newPage) => {
  currentPage.value = newPage;
  // 保持当前选中的患者ID
  const selectedId = currentPatient.value ? currentPatient.value.id : null;
  fetchPatients(selectedId);
};

const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1;
  // 保持当前选中的患者ID
  const selectedId = currentPatient.value ? currentPatient.value.id : null;
  fetchPatients(selectedId);
};

// 添加编辑患者方法
const handleEditPatient = (patient) => {
  isEditing.value = true;
  // 复制患者信息到表单
  newPatientForm.value = { ...patient };
  dialogVisible.value = true;
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
    await ElMessageBox.confirm(
      '确定要删除该患者吗？删除后将同时删除该患者的所有分析报告数据，且无法恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    // 删除患者
    const response = await request.delete(`/patients/${patient.id}`);
    
    if (response.code === 200) {
      ElMessage.success('删除成功');
      // 如果删除的是当前选中的患者，清空当前患者并重置报告状态
      if (currentPatient.value && currentPatient.value.id === patient.id) {
        currentPatient.value = null;
        hasAnalysisData.value = false;
        showReport.value = false; // 确保不显示报告页面
      }
      // 重新获取患者列表
      await fetchPatients();
    } else {
      ElMessage.error(response.message || '删除失败');
    }
  } catch (error) {
    // 区分取消操作和真正的错误
    if (error !== 'cancel') {
      console.error('删除患者出错:', error); // 添加详细错误信息
      ElMessage.error('删除患者失败');
    }
  }
};

const handleAddPatient = () => {
  // 表单验证
  patientFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请正确填写表单信息');
      return;
    }
    
    try {
      // 表单验证通过，继续处理
      const formData = { ...newPatientForm.value };
      
      // 根据是否编辑模式选择不同的API
      const url = isEditing.value ? `/patients/${formData.id}` : '/patients';
      const method = isEditing.value ? 'put' : 'post';
      
      const response = await request[method](url, formData);
      
      if (response) {
        ElMessage.success(isEditing.value ? '患者信息更新成功' : '患者添加成功');
        dialogVisible.value = false;
        
        // 重置表单
        patientFormRef.value.resetFields();
        
        // 刷新患者列表
        fetchPatients();
      }
    } catch (error) {
      console.error('保存患者信息失败:', error);
      ElMessage.error(error.response?.data?.message || '操作失败，请稍后重试');
    }
  });
};

// 添加从身份证提取年龄的辅助函数
const calculateAgeFromIdCard = (idCard) => {
  if (!idCard || idCard.length !== 18) return 0;
  
  try {
    // 从身份证提取出生年月日 (格式: YYYYMMDD)
    const birthYear = parseInt(idCard.substring(6, 10));
    const birthMonth = parseInt(idCard.substring(10, 12));
    const birthDay = parseInt(idCard.substring(12, 14));
    
    const today = new Date();
    const birthDate = new Date(birthYear, birthMonth - 1, birthDay);
    
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    // 如果生日还没过，年龄减1
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    
    return age;
  } catch (e) {
    console.error('计算年龄出错:', e);
    return 0;
  }
};

// 修改身份证号变更处理函数
const handleIdCardChange = () => {
  if (newPatientForm.value.idCard) {
    // 自动提取性别
    const idCard = newPatientForm.value.idCard;
    if (idCard.length === 18) {
      const genderBit = parseInt(idCard.charAt(16));
      newPatientForm.value.gender = genderBit % 2 === 0 ? '女' : '男';
      
      // 自动提取年龄
      const birthYear = idCard.substring(6, 10);
      const birthMonth = idCard.substring(10, 12);
      const birthDay = idCard.substring(12, 14);
      
      const birthDate = new Date(`${birthYear}-${birthMonth}-${birthDay}`);
      const today = new Date();
      let age = today.getFullYear() - birthDate.getFullYear();
      
      // 如果今年的生日还没过，年龄减1
      if (today.getMonth() < birthDate.getMonth() || 
          (today.getMonth() === birthDate.getMonth() && today.getDate() < birthDate.getDate())) {
        age--;
      }
      
      // 更新年龄字段（如果表单中有这个字段）
      if ('age' in newPatientForm.value) {
        newPatientForm.value.age = age;
      }
    }
  }
};

// 重置表单的方法
const resetForm = () => {
  newPatientForm.value = {
    name: '',
    gender: '',
    idCard: '',
    medicalNumber: '',
    phone: '',
    address: '',
    cardNumber: ''
  };
  isEditing.value = false;
};


const handlePreview = () => {
  // 仅处理预览逻辑，不调用后端保存
  console.log('预览照片');
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
const fetchAllPatients = async () => {
  try {
    const response = await request.get('/patients/all');
    const patientData = response.content || [];
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
  // 先将所有患者的active设为false
  patients.value.forEach(p => p.active = false);
  
  const processedPatient = {
    ...patient,
    age: patient.age ?? '未知',
    phone: patient.phone ?? '未填写',
    address: patient.address ?? '未填写',
    cardNumber: patient.cardNumber ?? '未填写',
    active: true  // 直接在处理后的患者对象中设置 active 状态
  };

  // 更新当前选中的患者
  currentPatient.value = processedPatient;
  
  // 更新列表中对应患者的状态
  const index = patients.value.findIndex(p => p.id === patient.id);
  if (index !== -1) {
    patients.value[index].active = true;
  }
  
  // 重置当前报告数据和按钮状态
  currentReport.value = null;
  hasAnalysisData.value = false;
  
  // 检查是否有分析报告
  checkHasAnalysisReport(patient.id);
  
  // 如果当前在报告页，切换到主页
  if (showReport.value) {
    showReport.value = false;
  }
};

// 添加一个新方法，用于检查患者是否有分析报告
const checkHasAnalysisReport = async (patientId) => {
  try {
    const response = await request.get(`/analysis-reports/patient/${patientId}/latest`, {
      params: { reportType: 'TONGUE' }
    });
    
    // 如果有报告数据，则更新状态
    if (response && response.id) {
      hasAnalysisData.value = true;
      currentReport.value = response;
    } else {
      // 如果没有报告数据，隐藏分析报告按钮
      hasAnalysisData.value = false;
      currentReport.value = null;
      
      // 如果当前在报告页，切换到主页
      if (showReport.value) {
        showReport.value = false;
      }
    }
  } catch (error) {
    console.error('检查分析报告失败:', error);
    hasAnalysisData.value = false;
    currentReport.value = null;
    
    // 发生错误时，如果在报告页，切换到主页
    if (showReport.value) {
      showReport.value = false;
    }
  }
};

// 导入图片
// 修改图片引用方式。在 <mcfile name="web/src/views/dashboard/index.vue" path="d:\software\myworkspace_projects\mes-System\web\src\views\dashboard\index.vue"> 中：
const collectionItems = ref([
  {
    title: "面色采集",
    buttonText: "面色分析",
    image: new URL('@/assets/images/face.jpg', import.meta.url).href,
  },
  {
    title: "舌象采集",
    buttonText: "舌象分析",
    image: new URL('@/assets/images/tongue.jpg', import.meta.url).href,
  },
]);

const avatarImage = new URL('@/assets/images/avatar.jpg', import.meta.url).href;

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


// 处理打开相机
const handleOpenCamera = (type) => {
  if (!currentPatient.value) {
    ElMessage.warning('请先选择患者');
    return;
  }
  
  // 如果是面色分析，显示开发中提示
  if (type === '面色分析') {
    ElMessage({
      message: '面色分析功能正在开发中...',
      type: 'info',
      duration: 3000
    });
    return;
  }
  
  // 其他类型（如舌象分析）正常打开相机
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
    
    // 这个函数现在仅在非分析场景下使用，例如手动上传图片
    // 这种情况下实现可以保留
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
      
      // 关闭相机对话框
      showCamera.value = false;
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

// 添加获取最新分析报告的方法
const fetchLatestAnalysisReport = async (patientId, reportType, showSuccessMessage = false) => {
  try {
    // 显示加载指示器
    const loading = ElLoading.service({
      lock: true,
      text: '加载分析报告...',
      background: 'rgba(0, 0, 0, 0.7)'
    });

    const response = await request.get(`/analysis-reports/patient/${patientId}/latest`, {
      params: { reportType }
    });
    
    // 隐藏加载指示器
    loading.close();
    
    if (response && response.id) {
      // 更新分析数据状态
      hasAnalysisData.value = true;
      
      // 更新当前报告数据，传递给分析报告组件
      currentReport.value = response;
    } else {
      // 如果没有找到报告，可以显示提示或重置状态
      hasAnalysisData.value = false;
      currentReport.value = null;
      
      // 显示提示并返回主页
      ElMessage.info('该患者暂无分析报告');
      // 关闭报告页面，返回主页
      showReport.value = false;
    }
  } catch (error) {
    console.error('获取分析报告失败:', error);
    
    // 隐藏加载指示器
    ElLoading.service().close();
    
    // 出错时重置状态
    hasAnalysisData.value = false;
    currentReport.value = null;
    
    // 显示提示并返回主页
    ElMessage.error('获取分析报告失败');
    // 关闭报告页面
    showReport.value = false;
  }
};

// 添加处理分析结果的方法
const handleAnalyzeResult = async (result, imageData) => {
  console.log('收到分析结果:', result);
  
  // 已经在/prediction接口中完成了图像的保存和分析
  // 不需要再次调用/patients/${patientId}/images接口
  
  // 如果结果中包含问卷数据
  if (result.questionnaire) {
    // 更新患者的体质信息
    const constitutionData = {
      patientId: currentPatient.value.id,
      mainType: result.questionnaire.mainType,
      secondaryType: result.questionnaire.secondaryType,
      description: getConstitutionDescription(result.questionnaire.mainType)
    };
    
    // 更新当前患者的体质分析数据
    if (!currentPatient.value.constitutionAnalysis) {
      currentPatient.value.constitutionAnalysis = {};
    }
    
    currentPatient.value.constitutionAnalysis = {
      ...currentPatient.value.constitutionAnalysis,
      ...constitutionData
    };
    
    // 保存体质数据到后端
    saveConstitutionData(constitutionData)
      .then(() => {
        console.log('体质数据保存成功');
      })
      .catch(error => {
        console.error('体质数据保存失败:', error);
      });
  }
  
  // 如果是舌象分析，处理舌象数据
  if (captureType.value === '舌象分析' && result.tongueAnalysis) {
    // 确保舌象分析对象存在
    if (!currentPatient.value.tongueAnalysis) {
      currentPatient.value.tongueAnalysis = {};
    }
    
    // 更新舌象分析数据
    currentPatient.value.tongueAnalysis = {
      ...currentPatient.value.tongueAnalysis,
      ...result.tongueAnalysis
    };
    
    // 更新舌象图像
    if (result.tongueImage) {
      currentPatient.value.tongueImage = result.tongueImage;
    }
    
    if (result.tongueMaskImage) {
      currentPatient.value.tongueMaskImage = result.tongueMaskImage;
    }
  }
  
  // 设置分析报告按钮可见
  hasAnalysisData.value = true;
  
  // 关闭相机对话框
  showCamera.value = false;
  
  // 分析完成后，获取最新的分析报告数据
  if (currentPatient.value && currentPatient.value.id) {
    checkHasAnalysisReport(currentPatient.value.id);
  }
};

// 获取体质描述的辅助函数
const getConstitutionDescription = (constitutionType) => {
  const descriptions = {
    '气虚质': '气虚质的人容易疲劳，气短懒言，动则气喘，精神不振，注意力不集中，适应运动，避免过度劳累。',
    '血虚质': '血虚质的人容易面色苍白，头晕眼花，指甲色淡，唇色淡白，注意保持良好的饮食习惯，多吃富含铁和蛋白质的食物。',
    '阴虚质': '阴虚质的人容易手脚心热，口干咽干，皮肤干燥，大便干结，注意保持足够的水分摄入，避免辛辣刺激性食物。'
  };
  
  return descriptions[constitutionType] || '请根据中医师建议，进行针对性的调理和保健。';
};

// 保存体质数据到后端
const saveConstitutionData = async (data) => {
  try {
    const response = await request.post(`/patients/${data.patientId}/constitution`, data);
    return response;
  } catch (error) {
    console.error('保存体质数据出错:', error);
    throw error;
  }
};

// 组件挂载时检查登录状态并获取数据
onMounted(() => {
  if (checkAuth()) {
    fetchPatients().then(() => {
      // 初始化完成后，如果有选中的患者，检查是否有分析报告
      if (currentPatient.value && currentPatient.value.id) {
        checkHasAnalysisReport(currentPatient.value.id);
      }
    });
  }
  window.addEventListener('keydown', handleKeyDown);
});

// 在组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
  if (scanTimeout) {
    clearTimeout(scanTimeout);
    scanTimeout = null;
  }
});
</script>

<style scoped>
.pagination-custom {
  --el-pagination-button-disabled-bg-color: #f5f7fa;
  --el-pagination-bg-color: #ffffff;
  --el-pagination-button-bg-color: #ffffff;
  --el-pagination-button-color: #606266;
  --el-pagination-hover-color: #409eff;
  padding: 4px 0;
  width: 80%;
  max-width: 200px;
  margin: 0 auto;
  display: flex !important;
  justify-content: center;
  align-items: center;
}

.pagination-custom :deep(.el-pagination) {
  justify-content: center;
  white-space: nowrap;
}

.pagination-custom :deep(.el-pagination.is-background .btn-next),
.pagination-custom :deep(.el-pagination.is-background .btn-prev),
.pagination-custom :deep(.el-pagination.is-background .el-pager li) {
  margin: 0 2px;
  min-width: 24px;
  height: 24px;
  line-height: 24px;
  font-size: 12px;
}

/* 扫码狂 */
.scan-dialog :deep(.el-dialog) {
  border-radius: 0;
  margin-top: 15vh !important;
}

.scan-dialog :deep(.el-dialog__header) {
  background-color: #40a9ff;
  margin: 0;
  padding: 8px 15px;
  position: relative;
}

.scan-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 15px;
  font-weight: normal;
}
/*隐藏主内容区域滚动条*/
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.scan-dialog :deep(.el-dialog__headerbtn) {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  padding: 0;
  background: #ff4d4f;
  border-radius: 0;
}

.scan-dialog :deep(.el-dialog__close) {
  color: white !important;
  font-weight: bold;
  font-size: 14px;
}

.scan-dialog :deep(.el-dialog__body) {
  padding: 15px;
  background-color: #f5f5f5;
}

.scan-dialog :deep(.el-input__inner) {
  border-radius: 0;
  height: 28px;
  border: 1px solid #d9d9d9;
}

.scan-button {
  height: 28px;
  background-color: #40a9ff;
  border: none;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
  padding: 0 15px;
}

.scan-button:hover {
  background-color: #1890ff;
}

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
