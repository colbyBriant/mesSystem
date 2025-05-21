import request from '@/utils/request'

// 保存诊断图片到数据库和本地
export function saveDiagnosisImage(patientId, imageData, imageType) {
  return request({
    url: `/patients/${patientId}/images`,
    method: 'post',
    data: {
      [imageType === 'FACE' ? 'faceImage' : 'tongueImage']: imageData,
      [imageType === 'FACE' ? 'faceAnalysis' : 'tongueAnalysis']: 
        `${imageType === 'FACE' ? '面色' : '舌象'}分析结果`
    }
  })
}

// 查询诊断图片
export function getDiagnosisImages(patientId, type) {
  return request({
    url: `/patients/${patientId}/images`,
    method: 'get',
    params: {
      type: type.toLowerCase()
    }
  })
}