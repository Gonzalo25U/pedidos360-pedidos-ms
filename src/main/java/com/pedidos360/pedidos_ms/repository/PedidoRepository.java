package com.pedidos360.pedidos_ms.repository;

import com.pedidos360.pedidos_ms.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(String usuarioId);

    Optional<Pedido> findByIdAndUsuarioId(Long id, String usuarioId);
}
