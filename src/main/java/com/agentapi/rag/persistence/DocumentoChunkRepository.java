package com.agentapi.rag.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentoChunkRepository extends JpaRepository<DocumentoChunkEntity, UUID> {

    @Query("""
            SELECT c FROM DocumentoChunkEntity c
            WHERE LOWER(c.dsConteudo) LIKE LOWER(CONCAT('%', :term, '%'))
            ORDER BY c.nuOrdem ASC
            """)
    List<DocumentoChunkEntity> searchByContentLike(@Param("term") String term, Pageable pageable);
}
