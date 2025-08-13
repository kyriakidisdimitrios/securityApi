document.addEventListener("DOMContentLoaded", function () {
    const captchaImg = document.getElementById("captchaImage");
    const refreshBtn = document.getElementById("refreshCaptcha");

    if (captchaImg && refreshBtn) {
        refreshBtn.addEventListener("click", function () {
            captchaImg.src = "/captcha-image?" + new Date().getTime(); // prevent caching
        });
    }
});