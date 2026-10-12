function buscarEstudiante() {
    const query = document.getElementById('buscarEstudianteInput').value.trim();
    const resultadoDiv = document.getElementById('resultadoBusqueda');
    const seccionTexto = document.getElementById('seccionEstudianteTexto');

    resultadoDiv.innerHTML = '';
    seccionTexto.textContent = '';

    if (query === '') {
        return;
    }

    fetch('/estudiantes/buscar-general?q=' + encodeURIComponent(query))
        .then(response => response.json())
        .then(data => {
            if (data.tipo === 'unico') {
                aplicarEstudiante(data);
            } else if (data.tipo === 'lista') {
                let html = '<ul style="list-style:none; padding:0; margin:0;">';
                data.resultados.forEach(function (est) {
                    html += '<li style="padding:6px 0; border-bottom:1px solid #eee; cursor:pointer;" '
                        + 'onclick=\'seleccionarEstudiante(' + JSON.stringify(est) + ')\'>'
                        + '📌 ' + est.nombreCompleto + ' — NIE: ' + est.nie + ' — Sección: ' + (est.seccion || 'N/D')
                        + '</li>';
                });
                html += '</ul>';
                resultadoDiv.innerHTML = html;
            } else {
                resultadoDiv.innerHTML = '<p style="color:#C0392B;">⚠️ No se encontró ningún estudiante.</p>';
            }
        })
        .catch(error => console.error('Error buscando estudiante:', error));
}

function seleccionarEstudiante(est) {
    fetch('/estudiantes/buscar-general?q=' + encodeURIComponent(est.nie))
        .then(response => response.json())
        .then(data => {
            if (data.tipo === 'unico') {
                aplicarEstudiante(data);
                document.getElementById('resultadoBusqueda').innerHTML = '';
            }
        });
}

function aplicarEstudiante(data) {
    document.getElementById('nie').value = data.nie;
    document.getElementById('estudiante').value = data.nombreCompleto;

    const metodoSelect = document.getElementById('metodoPago');
    if (metodoSelect) {
        metodoSelect.value = 'efectivo';
    }

    document.getElementById('seccionEstudianteTexto').textContent =
        'Sección: ' + (data.seccion || 'N/D');
}

document.addEventListener('DOMContentLoaded', function () {
    const nieInput = document.getElementById('nie');
    const estudianteInput = document.getElementById('estudiante');

    if (nieInput) {
        nieInput.addEventListener('blur', function () {
            const nie = nieInput.value.trim();
            if (nie === '') {
                return;
            }
            fetch('/estudiantes/buscar-nie?nie=' + encodeURIComponent(nie))
                .then(response => response.json())
                .then(data => {
                    if (data.encontrado) {
                        estudianteInput.value = data.nombreCompleto;
                        document.getElementById('seccionEstudianteTexto').textContent =
                            'Sección: ' + (data.seccion || 'N/D');
                    }
                })
                .catch(error => console.error('Error buscando estudiante:', error));
        });
    }
});