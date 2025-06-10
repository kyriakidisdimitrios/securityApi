//console.log('🟢 Cart JS loaded!');
document.addEventListener('DOMContentLoaded', () => {
    // Quantity update via AJAX
    document.querySelectorAll('.cart-qty-input').forEach(input => {
        input.addEventListener('change', function () {
            const cartItemId = this.getAttribute('data-cart-id');
            const quantity = this.value;

            console.log('Updating cart item:', cartItemId, 'to quantity:', quantity);

            fetch('/cart/update-ajax', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ cartItemId, quantity })
            })
                .then(res => {
                    if (!res.ok) throw new Error("Failed to update");
                    return res.json();
                })
                .then(data => {
                    console.log('Update response:', data);
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
            console.log('Attempting to remove cart item:', cartItemId);

            if (!confirm("Are you sure you want to remove this item?")) return;

            fetch('/cart/remove-ajax', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ cartItemId })
            })
                .then(res => {
                    if (!res.ok) throw new Error("Failed to remove");
                    return res.json();
                })
                .then(data => {
                    console.log('Remove response:', data);
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
});