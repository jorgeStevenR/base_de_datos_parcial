package com.uniminuto.auth.domain.repository;

import com.uniminuto.auth.domain.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findAllByOrderByFechaVentaDesc();

    List<Venta> findByFechaVentaBetweenOrderByFechaVentaDesc(
            LocalDateTime start, LocalDateTime end
    );
}
