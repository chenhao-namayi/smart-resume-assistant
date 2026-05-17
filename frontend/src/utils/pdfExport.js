import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

export async function exportToPDF(resumeData, filename = 'resume.pdf') {
  if (!resumeData) {
    console.error('No resume data')
    return
  }

  // Show a loading overlay while rendering
  const overlay = document.createElement('div')
  overlay.style.cssText = 'position:fixed;inset:0;z-index:99999;background:rgba(0,0,0,0.5);display:flex;align-items:center;justify-content:center;'
  overlay.innerHTML = '<p style="color:#fff;font-size:18px;">正在生成PDF...</p>'
  document.body.appendChild(overlay)

  // Create the resume page element — fully visible for html2canvas
  const page = document.createElement('div')
  page.style.cssText = 'position:fixed;left:50%;top:2%;z-index:100000;transform:translateX(-50%);width:794px;background:#fff;box-shadow:0 4px 24px rgba(0,0,0,0.3);border-radius:2px;'
  page.innerHTML = buildTemplate(resumeData)
  document.body.appendChild(page)

  // Let the browser lay out the content
  await new Promise(r => setTimeout(r, 300))

  try {
    const canvas = await html2canvas(page, {
      scale: 2,
      useCORS: true,
      logging: false,
      backgroundColor: '#ffffff'
    })

    document.body.removeChild(page)
    overlay.innerHTML = '<p style="color:#fff;font-size:18px;">正在保存...</p>'

    const imgWidth = 210
    const pageHeight = 297
    const imgHeight = (canvas.height * imgWidth) / canvas.width

    const pdf = new jsPDF('p', 'mm', 'a4')
    let heightLeft = imgHeight
    let position = 0

    pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    while (heightLeft > 0) {
      position -= pageHeight
      pdf.addPage()
      pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    pdf.save(filename)
  } catch (error) {
    console.error('PDF export failed:', error)
    throw error
  } finally {
    if (document.body.contains(page)) document.body.removeChild(page)
    document.body.removeChild(overlay)
  }
}

function safeArray(val) {
  if (Array.isArray(val)) return val
  return []
}

function safeStr(val) {
  if (typeof val === 'string') return val
  return ''
}

function buildTemplate(data) {
  const basic = data.basicInfo || {}
  const name = basic.name || '未命名'
  const phone = basic.phone || ''
  const email = basic.email || ''
  const position = basic.position || ''
  const photo = basic.photo || ''

  const contacts = [phone, email].filter(Boolean).join('  |  ')

  const summary = safeStr(data.summary)
  const skills = safeStr(data.skills)
  const work = safeArray(data.workExperience)
  const edu = safeArray(data.education)
  const projects = safeArray(data.projects)

  const css = `
    *{margin:0;padding:0;box-sizing:border-box}
    body{font-family:"PingFang SC","Microsoft YaHei","Noto Sans SC",sans-serif;color:#2c2c2c;background:#fff;line-height:1.65;font-size:13px}
    .page{width:794px;padding:52px 60px}
    .hdr{display:flex;align-items:center;justify-content:space-between;margin-bottom:36px;min-height:120px}
    .hdr .info{flex:1;text-align:center}
    .hdr .info h1{font-size:28px;font-weight:700;letter-spacing:3px;color:#111;margin-bottom:8px}
    .hdr .info .ctc{font-size:13px;color:#777;letter-spacing:1px}
    .hdr .photo-box{width:100px;height:130px;border:1px solid #ccc;border-radius:4px;overflow:hidden;flex-shrink:0;margin-left:20px}
    .hdr .photo-box img{width:100%;height:100%;object-fit:cover}
    .sec{margin-bottom:26px}
    .sec h2{font-size:15px;font-weight:700;color:#111;border-bottom:1.5px solid #444;padding-bottom:7px;margin-bottom:12px;letter-spacing:2px}
    .sec p,.sec .desc{font-size:13px;color:#444;white-space:pre-wrap}
    .item{margin-bottom:16px}
    .item .row{display:flex;justify-content:space-between;align-items:baseline;margin-bottom:3px}
    .item .row .l{font-size:14px;font-weight:600;color:#1a1a1a}
    .item .row .r{font-size:11px;color:#999;white-space:nowrap}
    .item .sub{font-size:13px;color:#666;margin-bottom:3px}
  `

  let html = `<style>${css}</style><div class="page">`

  html += `<div class="hdr">`
  html += `<div class="info"><h1>${esc(name)}</h1><p class="ctc">${esc(contacts)}</p>`
  if (position) html += `<p class="ctc" style="margin-top:4px;color:#555;font-size:14px;">${esc(position)}</p>`
  html += `</div>`
  if (photo) html += `<div class="photo-box"><img src="${esc(photo)}" alt="证件照" /></div>`
  html += `</div>`

  if (summary) html += `<div class="sec"><h2>个人简介</h2><p>${esc(summary)}</p></div>`

  if (work.length > 0) {
    html += `<div class="sec"><h2>工作经历</h2>`
    for (const item of work) {
      if (typeof item !== 'object' || !item) continue
      html += `<div class="item"><div class="row"><span class="l">${esc(item.company)}</span><span class="r">${esc(item.period || '')}</span></div><div class="sub">${esc(item.position)}</div><p class="desc">${esc(item.description)}</p></div>`
    }
    html += `</div>`
  }

  if (edu.length > 0) {
    html += `<div class="sec"><h2>教育背景</h2>`
    for (const item of edu) {
      if (typeof item !== 'object' || !item) continue
      html += `<div class="item"><div class="row"><span class="l">${esc(item.school)}</span><span class="r">${esc(item.period || '')}</span></div><div class="sub">${esc(item.degree)}${item.major ? '  |  ' + esc(item.major) : ''}</div></div>`
    }
    html += `</div>`
  }

  if (skills) html += `<div class="sec"><h2>专业技能</h2><p>${esc(skills)}</p></div>`

  if (projects.length > 0) {
    html += `<div class="sec"><h2>项目经历</h2>`
    for (const item of projects) {
      if (typeof item !== 'object' || !item) continue
      html += `<div class="item"><div class="row"><span class="l">${esc(item.name)}</span><span class="r">${esc(item.role)}</span></div><p class="desc">${esc(item.description)}</p></div>`
    }
    html += `</div>`
  }

  html += `</div>`
  return html
}

function esc(str) {
  if (!str) return ''
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}
