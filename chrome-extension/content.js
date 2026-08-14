(() => {
  if (window.__quickAnswerOverlayInstalled) return;
  window.__quickAnswerOverlayInstalled = true;

  let host = null;
  let timer = null;
  let drag = null;

  function show(text) {
    clearTimeout(timer);
    if (!host || !host.isConnected) createOverlay();

    const root = host.shadowRoot;
    const box = root.querySelector(".box");
    const label = root.querySelector(".answer");
    label.textContent = text;
    box.classList.add("visible");
    timer = setTimeout(() => box.classList.remove("visible"), 2000);
  }

  function createOverlay() {
    host = document.createElement("div");
    host.id = "__quick_answer_overlay_host";
    host.style.cssText = "all:initial;position:fixed;z-index:2147483647;top:90px;right:28px;";
    const root = host.attachShadow({ mode: "open" });
    root.innerHTML = `
      <style>
        .box {
          display:none; position:relative; min-width:120px; max-width:260px;
          padding:10px 14px 12px; border-radius:10px; color:#fff;
          background:#171923; border:1px solid #45495a;
          box-shadow:0 8px 30px rgba(0,0,0,.35);
          font:600 15px/1.35 system-ui,-apple-system,Segoe UI,sans-serif;
          cursor:move; user-select:none; overflow-wrap:anywhere;
        }
        .box.visible { display:block; }
        .top { display:flex; align-items:center; justify-content:space-between; gap:16px;
          margin-bottom:5px; color:#b8c0d9; font-size:10px; letter-spacing:.08em; text-transform:uppercase; }
        .answer { font-size:17px; font-weight:700; }
        .close { border:0; padding:0; color:#b8c0d9; background:transparent; cursor:pointer; font-size:16px; }
      </style>
      <div class="box" role="status" aria-live="polite">
        <div class="top"><span>Quick answer · 5 sec</span><button class="close" title="Close" aria-label="Close">×</button></div>
        <div class="answer"></div>
      </div>`;
    document.documentElement.appendChild(host);

    const box = root.querySelector(".box");
    root.querySelector(".close").addEventListener("click", () => {
      clearTimeout(timer);
      box.classList.remove("visible");
    });

    box.addEventListener("pointerdown", (event) => {
      if (event.target.closest(".close")) return;
      const rect = host.getBoundingClientRect();
      drag = { x: event.clientX, y: event.clientY, left: rect.left, top: rect.top };
      box.setPointerCapture?.(event.pointerId);
      event.preventDefault();
    });
    box.addEventListener("pointermove", (event) => {
      if (!drag) return;
      const left = Math.max(0, Math.min(window.innerWidth - host.offsetWidth, drag.left + event.clientX - drag.x));
      const top = Math.max(0, Math.min(window.innerHeight - host.offsetHeight, drag.top + event.clientY - drag.y));
      host.style.left = `${left}px`;
      host.style.top = `${top}px`;
      host.style.right = "auto";
    });
    box.addEventListener("pointerup", () => { drag = null; });
    box.addEventListener("pointercancel", () => { drag = null; });
  }

  chrome.runtime.onMessage.addListener((message) => {
    if (message?.type === "QA_PING") return;
    if (message?.type === "QA_SHOW_STATUS") show(message.text || "Working…");
    if (message?.type === "QA_SHOW_ANSWER") show(message.text || "No answer returned");
  });
})();