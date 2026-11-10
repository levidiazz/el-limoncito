document.addEventListener("DOMContentLoaded", function () {
    var ruta = window.location.pathname;
    var enlaces = document.querySelectorAll(".barra-cuerpo a.barra-enlace[href]");
    var mejor = null;
    enlaces.forEach(function (enlace) {
        var destino = new URL(enlace.href, window.location.origin).pathname;
        var coincide = destino === "/" ? ruta === "/" : ruta === destino || ruta.indexOf(destino + "/") === 0;
        if (coincide && (mejor === null || destino.length > mejor.destino.length)) {
            mejor = { enlace: enlace, destino: destino };
        }
    });
    if (mejor !== null) {
        mejor.enlace.classList.add("activo");
        mejor.enlace.setAttribute("aria-current", "page");
    }
    document.querySelectorAll("[data-imprimir]").forEach(function (boton) {
        boton.addEventListener("click", function () {
            window.print();
        });
    });
    var efectivo = document.querySelector("input[data-total]");
    var cambio = document.getElementById("cambio");
    if (efectivo !== null && cambio !== null) {
        var total = parseFloat(efectivo.getAttribute("data-total"));
        var formato = new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" });
        efectivo.addEventListener("input", function () {
            var recibido = parseFloat(efectivo.value);
            if (isNaN(recibido)) {
                cambio.textContent = formato.format(0);
                cambio.classList.remove("cifra-alerta");
                return;
            }
            var diferencia = Math.round((recibido - total) * 100) / 100;
            cambio.textContent = diferencia < 0 ? "Faltan " + formato.format(-diferencia) : formato.format(diferencia);
            cambio.classList.toggle("cifra-alerta", diferencia < 0);
        });
    }
    document.querySelectorAll("form[data-confirmar]").forEach(function (formulario) {
        formulario.addEventListener("submit", function (evento) {
            if (!window.confirm(formulario.getAttribute("data-confirmar"))) {
                evento.preventDefault();
            }
        });
    });
});
