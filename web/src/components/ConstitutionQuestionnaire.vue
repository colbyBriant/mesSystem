<template>
  <el-dialog
    v-model="dialogVisible"
    title="中医体质辨识问卷"
    width="800px"
    :before-close="handleClose"
    :close-on-click-modal="false"
  >
    <div class="constitution-questionnaire">
      <div class="questionnaire-header">
        <h3 class="text-center font-medium mb-4">中医体质辨识量表</h3>
        <p class="text-sm text-gray-500 mb-4">请根据近一年的体验和感受，对下列项目进行评价</p>
      </div>
      
      <div class="overflow-x-auto">
        <table class="questionnaire-table w-full border-collapse">
          <thead>
            <tr class="bg-gray-50">
              <th class="p-2 border text-center w-10">序号</th>
              <th class="p-2 border text-center">项目</th>
              <th class="p-2 border text-center w-20">A 没有</th>
              <th class="p-2 border text-center w-20">B 很少</th>
              <th class="p-2 border text-center w-28">C 有时</th>
              <th class="p-2 border text-center w-28">D 经常</th>
              <th class="p-2 border text-center w-28">E 总是</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in questionItems" :key="index" :class="{ 'bg-gray-50': index % 2 === 0 }">
              <td class="p-2 border text-center">{{ index + 1 }}</td>
              <td class="p-2 border text-left">{{ item.question }}</td>
              <td class="p-2 border text-center">
                <el-radio v-model="item.answer" label="A" size="small" />
              </td>
              <td class="p-2 border text-center">
                <el-radio v-model="item.answer" label="B" size="small" />
              </td>
              <td class="p-2 border text-center">
                <el-radio v-model="item.answer" label="C" size="small" />
              </td>
              <td class="p-2 border text-center">
                <el-radio v-model="item.answer" label="D" size="small" />
              </td>
              <td class="p-2 border text-center">
                <el-radio v-model="item.answer" label="E" size="small" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="flex justify-center mt-6 space-x-4">
        <el-button type="primary" @click="handleSubmit">提交问卷</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  patientId: {
    type: [String, Number],
    default: ''
  }
});

const emit = defineEmits(['close', 'submit']);

const dialogVisible = ref(false);

// 监听外部传入的visible属性
watch(() => props.visible, (newValue) => {
  dialogVisible.value = newValue;
});

// 监听内部dialogVisible变化，通知父组件
watch(dialogVisible, (newValue) => {
  if (!newValue) {
    emit('close');
  }
});

// 问题列表 - 按照体质评判标准修改类型
const questionItems = ref([
  { 
    question: '您活动后容易出虚汗?', 
    answer: null,
    type: '气虚质',
    index: 1
  },
  { 
    question: '您比别人容易疲乏?', 
    answer: null,
    type: '气虚质',
    index: 2
  },
  { 
    question: '您容易感冒?', 
    answer: null,
    type: '阳虚质',
    index: 3
  },
  { 
    question: '您平时说话声音低弱无力?', 
    answer: null,
    type: '阳虚质',
    index: 4
  },
  { 
    question: '您面色苍白?', 
    answer: null,
    type: '血虚质',
    index: 5
  },
  { 
    question: '您指甲色淡白?', 
    answer: null,
    type: '血虚质',
    index: 6
  },
  { 
    question: '您容易有黑眼圈?', 
    answer: null,
    type: '阴虚质',
    index: 7
  },
  { 
    question: '您容易头晕或站起时眩晕?', 
    answer: null,
    type: '阴虚质',
    index: 8
  },
  { 
    question: '您性格多沉静?', 
    answer: null,
    type: '痰湿质',
    index: 9
  },
  { 
    question: '您手脚心热?', 
    answer: null,
    type: '痰湿质',
    index: 10
  },
  { 
    question: '您感到口干咽干?', 
    answer: null,
    type: '湿热质',
    index: 11
  },
  { 
    question: '您皮肤或口唇干?', 
    answer: null,
    type: '湿热质',
    index: 12
  },
  { 
    question: '您容易大便干结?', 
    answer: null,
    type: '血淤质',
    index: 13
  },
  { 
    question: '您舌苔少或没有舌苔?', 
    answer: null,
    type: '血淤质',
    index: 14
  },
  { 
    question: '您容易焦虑或烦躁?', 
    answer: null,
    type: '气郁质',
    index: 15
  }
]);

// 处理关闭弹窗
const handleClose = () => {
  // 添加日志
  console.log('问卷对话框关闭');
  dialogVisible.value = false;
};

// 处理取消
const handleCancel = () => {
  // 添加日志
  console.log('问卷取消按钮点击');
  dialogVisible.value = false;
};

// 处理提交
const handleSubmit = () => {
  // 添加日志
  console.log('问卷提交按钮点击');
  
  // 检查是否所有问题都已回答
  const unanswered = questionItems.value.filter(item => !item.answer);
  if (unanswered.length > 0) {
    ElMessage.warning(`还有 ${unanswered.length} 个问题未回答，请完成所有问题。`);
    return;
  }
  
  // 获取问卷回答
  const answers = questionItems.value.map(item => ({
    index: item.index,
    question: item.question,
    answer: item.answer,
    type: item.type
  }));
  
  // 判断体质类型 - 根据D、E的频率决定体质
  // 统计各个类型的D、E答案数量
  const typeCountsDE = {};
  
  // 初始化各类型计数
  const allTypes = ['气虚质', '阳虚质', '血虚质', '阴虚质', '痰湿质', '湿热质', '血淤质', '气郁质', '平和质'];
  allTypes.forEach(type => {
    typeCountsDE[type] = 0;
  });
  
  // 计算各类型D、E答案的数量
  questionItems.value.forEach(item => {
    if (item.answer === 'D' || item.answer === 'E') {
      typeCountsDE[item.type]++;
    }
  });
  
  // 判断是否为平和质（所有题都选A）
  const isBalanced = questionItems.value.every(item => item.answer === 'A');
  
  let mainType = '平和质';
  let secondaryType = null;
  
  if (isBalanced) {
    mainType = '平和质';
  } else {
    // 判断主要体质类型
    // 气虚质：1、2题均选D或E
    if (typeCountsDE['气虚质'] === 2) {
      mainType = '气虚质';
      
      // 气虚伴阳虚质：1、2、3、4题都为D或E
      if (typeCountsDE['阳虚质'] === 2) {
        secondaryType = '阳虚质';
      }
    } 
    // 判断其他体质类型
    else {
      // 根据D、E频率最高的确定主体质
      let maxCount = 0;
      for (const [type, count] of Object.entries(typeCountsDE)) {
        if (type !== '平和质' && count > maxCount) {
          maxCount = count;
          mainType = type;
        }
      }
      
      // 寻找次高频率的类型作为次要体质
      let secondMaxCount = 0;
      for (const [type, count] of Object.entries(typeCountsDE)) {
        if (type !== mainType && type !== '平和质' && count > secondMaxCount) {
          secondMaxCount = count;
          secondaryType = type;
        }
      }
    }
  }
  
  // 准备提交结果
  const result = {
    patientId: props.patientId,
    answers: answers,
    mainType: mainType,
    secondaryType: secondaryType || mainType,
    constitutionCounts: typeCountsDE,
    isBalanced: isBalanced
  };
  
  // 通知父组件
  emit('submit', result);
  
  // 关闭弹窗
  dialogVisible.value = false;
  
  // 显示成功提示
  ElMessage.success('提交成功！');
};
</script>

<style scoped>
.constitution-questionnaire {
  max-height: 70vh;
  overflow-y: auto;
}

.questionnaire-table {
  border: 1px solid #ebeef5;
  font-size: 0.875rem;
}

.questionnaire-table th,
.questionnaire-table td {
  border: 1px solid #ebeef5;
}

:deep(.el-radio__input) {
  margin-right: 0;
}
</style> 