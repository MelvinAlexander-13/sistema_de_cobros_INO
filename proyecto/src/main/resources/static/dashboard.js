document.addEventListener('DOMContentLoaded', function () {

    const datosEl = document.getElementById('datosDashboard');
    const porcentajeActivos = parseFloat(datosEl.dataset.porcentajeActivos);

    // Calculamos el promedio de cumplimiento leyendo los valores ya dibujados en pantalla
    const valoresBarras = Array.from(document.querySelectorAll('.bar-value'))
        .map(el => parseFloat(el.textContent));

    const promedio = valoresBarras.length
        ? Math.round((valoresBarras.reduce((a, b) => a + b, 0) / valoresBarras.length) * 10) / 10
        : 0;

    dibujarGauge('gaugeCumplimiento', promedio, '#22d3a4');
    document.getElementById('gaugeCumplimientoLabel').textContent = promedio + '%';

    dibujarGauge('gaugeActivos', porcentajeActivos, '#4f7cff');
    document.getElementById('gaugeActivosLabel').textContent = porcentajeActivos + '%';

    function dibujarGauge(canvasId, valor, color) {
        const ctx = document.getElementById(canvasId).getContext('2d');

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                datasets: [{
                    data: [valor, 100 - valor],
                    backgroundColor: [color, '#1e293b'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '75%',
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: false }
                }
            }
        });
    }

});