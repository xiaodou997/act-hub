/**
 * 将图片文件转换为WebP格式
 * @param {File} imageFile - 原始图片文件
 * @param {number} quality - WebP压缩质量 (0-1, 默认0.8)
 * @returns {Promise<File>} 转换后的WebP文件
 */
export const convertImageToWebp = (imageFile, quality = 0.8) => {
  return new Promise((resolve, reject) => {
    // 检查文件是否为图片
    if (!imageFile || !imageFile.type.startsWith('image/')) {
      resolve(imageFile) // 如果不是图片或为空，直接返回原文件
      return
    }

    const reader = new FileReader()

    reader.onload = (e) => {
      const img = new Image()

      img.onload = () => {
        // 创建Canvas元素
        const canvas = document.createElement('canvas')
        canvas.width = img.width
        canvas.height = img.height

        // 绘制图片到Canvas
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0)

        // 将Canvas内容转换为WebP格式的Blob
        canvas.toBlob(
          (blob) => {
            if (!blob) {
              resolve(imageFile) // 转换失败时返回原文件
              return
            }

            // 创建新的File对象
            const webpFile = new File([blob], `${imageFile.name.split('.')[0]}.webp`, {
              type: 'image/webp',
            })

            resolve(webpFile)
          },
          'image/webp',
          quality,
        )
      }

      img.onerror = () => {
        resolve(imageFile) // 图片加载失败时返回原文件
      }

      // 设置图片源
      img.src = e.target.result
    }

    reader.onerror = () => {
      resolve(imageFile) // 文件读取失败时返回原文件
    }

    // 读取文件
    reader.readAsDataURL(imageFile)
  })
}

/**
 * 批量转换图片文件为WebP格式
 * @param {File[]} files - 原始图片文件数组
 * @param {number} quality - WebP压缩质量
 * @returns {Promise<File[]>} 转换后的WebP文件数组
 */
export const batchConvertToWebp = async (files, quality = 0.8) => {
  const conversionPromises = files.map((file) => {
    if (file && file instanceof File) {
      return convertImageToWebp(file, quality)
    }
    return Promise.resolve(file)
  })

  return Promise.all(conversionPromises)
}
