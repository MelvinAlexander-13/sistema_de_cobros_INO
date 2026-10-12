document.addEventListener('keydown', function (e) {

    // Esc: cancelar / volver (solo si hay un boton de cancelar en pantalla)
    if (e.key === 'Escape') {
        const cancelar = document.querySelector('a.cancelar, a.btn-secondary');
        if (cancelar) {
            window.location.href = cancelar.href;
        }
        return;
    }

    if (!(e.altKey && e.shiftKey)) {
        return;
    }

    const rutas = {
        KeyD: '/usuarios/dashboard',
        KeyE: '/estudiantes',
        KeyP: '/pagos',
        KeyR: '/reportes'
    };

    if (rutas[e.code]) {
        e.preventDefault();
        window.location.href = rutas[e.code];
        return;
    }

    if (e.code === 'KeyN') {
        e.preventDefault();
        const enPagos = window.location.pathname.startsWith('/pagos');
        window.location.href = enPagos ? '/pagos' : '/estudiantes/nuevo';
        return;
    }

    if (e.code === 'KeyF') {
        e.preventDefault();
        const buscador = document.querySelector(
            '#buscarEstudianteInput, #buscarInput, input[name="texto"], input[name="nie"]'
        );
        if (buscador) {
            buscador.focus();
            buscador.select();
        }
        return;
    }

    if (e.code === 'KeyG') {
        e.preventDefault();
        const boton = document.querySelector('form button.guardar, form button[type="submit"].btn');
        if (boton) {
            boton.click();
        }
        return;
    }

    if (e.code === 'KeyH') {
        e.preventDefault();
        alert(
            'ATAJOS DE TECLADO\n\n' +
            'Alt+Shift+D  Dashboard\n' +
            'Alt+Shift+E  Estudiantes\n' +
            'Alt+Shift+P  Pagos\n' +
            'Alt+Shift+R  Reportes\n' +
            'Alt+Shift+N  Nuevo\n' +
            'Alt+Shift+F  Buscar\n' +
            'Alt+Shift+G  Guardar\n' +
            'Esc          Cancelar / volver'
        );
    }
});