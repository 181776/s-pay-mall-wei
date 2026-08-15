export function formatPrice(val) {
  if (typeof val === 'string') {
    if (isNaN(val)) return null
    const index = val.lastIndexOf('.')
    let p = ''
    if (index < 0) {
      p = val + '00'
    } else if (index === val.length - 2) {
      p = val.replace('.', '') + '0'
    } else {
      p = val.replace('.', '')
    }
    return parseInt(p, 10)
  }
  if (typeof val === 'number') {
    if (val == null) return null
    const s = String(val)
    if (s.length === 0) return '0.00'
    if (s.length === 1) return '0.0' + val
    if (s.length === 2) return '0.' + val
    const i = s.indexOf('.')
    if (i < 0) {
      return s.substring(0, s.length - 2) + '.' + s.substring(s.length - 2)
    }
    const num = s.substring(0, i) + s.substring(i + 1)
    if (i === 1) return '0.0' + num
    if (i === 2) return '0.' + num
    if (i > 2) return num.substring(0, i - 2) + '.' + num.substring(i - 2)
  }
  return val
}

export function displayPrice(cents) {
  const formatted = formatPrice(cents)
  if (formatted == null) return '0.00'
  return typeof formatted === 'number' ? formatted.toFixed(2) : formatted
}
