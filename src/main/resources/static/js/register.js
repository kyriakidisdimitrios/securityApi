//register.js
document.addEventListener('DOMContentLoaded', function () {
    console.log("register.js loaded");

    const input = document.getElementById('password');
    const bar   = document.getElementById('pwBar');
    const label = document.getElementById('pwLabel');

    if (!input || !bar || !label) return;

    const COMMON = new Set([
        "password","passw0rd","123456","123456789","qwerty",
        "iloveyou","admin","letmein","welcome","abc123"
    ]);

    function scorePassword(pw) {
        if (!pw) return 0;
        let score = 0;
        const len = pw.length;

        if (len >= 3) {
            const pct = Math.min(1, (len - 3) / 9);
            score += Math.floor(pct * 40);
        }

        const classes =
            (/[a-z]/.test(pw) ? 1 : 0) +
            (/[A-Z]/.test(pw) ? 1 : 0) +
            (/\d/.test(pw) ? 1 : 0) +
            (/[^A-Za-z0-9]/.test(pw) ? 1 : 0);
        score += classes * 10;

        score += Math.min(10, new Set(pw).size);

        if (/(.)\1\1/.test(pw)) score -= 15;
        if (COMMON.has(pw.toLowerCase())) score = Math.min(score, 10);

        return Math.max(0, Math.min(100, score));
    }

    function strengthLabel(score) {
        if (score < 25)  return { text: "Weak",   cls: "pw-weak",   width: "25%"  };
        if (score < 50)  return { text: "Fair",   cls: "pw-fair",   width: "50%"  };
        if (score < 75)  return { text: "Good",   cls: "pw-good",   width: "75%"  };
        return             { text: "Strong", cls: "pw-strong", width: "100%" };
    }

    function update() {
        const val = input.value || "";
        const score = scorePassword(val);
        const s = strengthLabel(score);
        bar.className = "pw-meter-bar " + s.cls;
        bar.style.width = s.width;
        label.textContent = "Strength: " + s.text;
    }

    function reflectValidity() {
        if (input.validity.valid) {
            label.classList.remove("text-danger");
        } else {
            label.classList.add("text-danger");
        }
    }

    input.addEventListener('input', () => { update(); reflectValidity(); });
    update();
    reflectValidity();
});
