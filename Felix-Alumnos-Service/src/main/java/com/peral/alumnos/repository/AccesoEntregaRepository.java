package com.peral.alumnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.peral.alumnos.model.AccesoEntrega;

import java.util.Optional;

@Repository
public interface AccesoEntregaRepository extends JpaRepository<AccesoEntrega, Long> {
    Optional<AccesoEntrega> findByShortId(String shortId);
}