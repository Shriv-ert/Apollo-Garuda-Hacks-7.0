export const EXTRACT_PROMPT = `Ekstrak SEMUA nomor telepon (misal: 08123456789, +62812...), nomor rekening bank, URL (domain/link), dan alamat email yang terlihat di dalam gambar ini.

Aturan:
- Balas HANYA JSON array of string. Tanpa penjelasan, tanpa markdown fence.
- Salin nilai apa adanya seperti tertulis di gambar.
- Jika tidak ada entitas sama sekali, balas: []

Contoh keluaran: ["08123456789","https://danakilat.xyz","1234567890"]`;
