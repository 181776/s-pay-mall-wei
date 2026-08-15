export const storage = {
  set(key, obj) {
    sessionStorage.setItem(key, JSON.stringify(obj))
  },
  get(key) {
    const raw = sessionStorage.getItem(key)
    if (!raw) return null
    try {
      return JSON.parse(raw)
    } catch {
      return null
    }
  },
  del(key) {
    sessionStorage.removeItem(key)
  },
}
