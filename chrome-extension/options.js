const input = document.getElementById("backend");
const status = document.getElementById("status");
chrome.storage.sync.get({ backendUrl: "https://quick-answer-backend-1-0-0.onrender.com/api/answer" }, (data) => {
  input.value = data.backendUrl;
});
document.getElementById("save").addEventListener("click", async () => {
  const value = input.value.trim();
  try {
    const url = new URL(value);
    if (!["http:", "https:"].includes(url.protocol)) throw new Error("Use an http or https URL.");
    await chrome.storage.sync.set({ backendUrl: value });
    status.textContent = "Saved";
  } catch (error) {
    status.textContent = error.message || "Invalid URL";
  }
});