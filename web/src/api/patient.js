import request from '@/utils/request'

// 获取分页患者列表
export function getPatients(page = 0, size = 10) {
  return request({
    url: '/patients',  // 去掉前面的/api
    method: 'get',
    params: { page, size }
  })
}

// 搜索患者
export function searchPatients(name, page = 0, size = 10) {
  return request({
    url: '/patients/search',  // 去掉前面的/api
    method: 'get',
    params: { name, page, size }
  })
}

// 获取单个患者
export function getPatient(id) {
  return request({
    url: `/patients/${id}`,
    method: 'get'
  })
}

// 更新患者面象图片
export function updatePatientFaceImage(patientId, imageData) {
  return request({
    url: `/patients/${patientId}/face-image`,
    method: 'put',
    data: { imageData }
  });
}

// 更新患者舌象图片
export function updatePatientTongueImage(patientId, imageData) {
  return request({
    url: `/patients/${patientId}/tongue-image`,
    method: 'put',
    data: { imageData }
  });
}

// 更新患者诊断图片
export function updatePatientDiagnosisImages(patientId, imageData) {
  return request({
    url: `/patients/${patientId}/diagnosis-images`,
    method: 'post',
    data: imageData
  });
}