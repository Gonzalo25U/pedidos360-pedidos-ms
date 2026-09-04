package com.pedidos360.pedidos_ms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Long id;
    private String usuarioId;
    private String estado;
    private BigDecimal total;
    private LocalDateTime creadoEn;
    private List<ItemPedidoDTO> items;
}
