package com.pedidos360.pedidos_ms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CrearPedidoRequest {

    @NotEmpty(message = "El pedido debe tener al menos un item")
    @Valid
    private List<ItemPedidoRequest> items;
}
