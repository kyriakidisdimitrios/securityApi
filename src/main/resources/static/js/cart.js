document.addEventListener('DOMContentLoaded', () => {
    // Quantity update via AJAX
    document.querySelectorAll('.cart-qty-input').forEach(input => {
        input.addEventListener('change', function () {
            const cartItemId = this.getAttribute('data-cart-id');
            const quantity = this.value;

            fetch('/cart/update-ajax', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ cartItemId, quantity })
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        location.reload();
                    } else {
                        alert(data.message || 'Update failed');
                    }
                })
                .catch(err => {
                    console.error('Update error:', err);
                    alert('Error updating cart');
                });
        });
    });

    // Remove item via AJAX
    document.querySelectorAll('.remove-cart-item').forEach(btn => {
        btn.addEventListener('click', function () {
            const cartItemId = this.getAttribute('data-cart-id');

            if (!confirm("Are you sure you want to remove this item?")) return;

            fetch('/cart/remove-ajax', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ cartItemId })
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        location.reload();
                    } else {
                        alert(data.message || 'Remove failed');
                    }
                })
                .catch(err => {
                    console.error('Remove error:', err);
                    alert('Error removing item');
                });
        });
    });

    // ✅ Card Integrity Switch logic
    const switchInput = document.getElementById("cardIntegritySwitch");
    const cardInput = document.getElementById("paymentInfo");
    if (switchInput && cardInput) {
        function updateCardValidation() {
            if (switchInput.checked) {
                cardInput.setAttribute("required", "required");
                cardInput.setAttribute("pattern", "\\d{16}");
            } else {
                cardInput.removeAttribute("required");
                cardInput.removeAttribute("pattern");
            }
        }

        switchInput.addEventListener("change", updateCardValidation);
        updateCardValidation(); // initial setup
    }
});

// ✅ Make openPopup globally accessible
function openPopup() {
    // Open the checkout popup window
    window.open('/cart/checkout-popup', 'checkoutPopup', 'width=600,height=400');

    // Redirect main window (not the popup) to /index after a brief delay
    setTimeout(() => {
        window.location.href = '/';
    }, 500); // Allow enough time for popup to initiate
}
