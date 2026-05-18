package com.uniminuto.auth.infrastructure.persistence;

import com.uniminuto.auth.domain.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
