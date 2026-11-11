package com.pedidos360.pedidos_ms.mensajeria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** Evento "pago.aprobado" publicado por pago-ms. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagoAprobadoEvent {
    private Long pagoId;
    private String buyOrder;
    private String usuarioId;
    private String emailUsuario;
    private BigDecimal total;
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Long productoId;
        private String nombreProducto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}