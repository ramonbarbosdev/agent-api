package com.agentapi.rag;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.agentapi.config.RagProperties;
import com.agentapi.rag.persistence.DocumentoChunkEntity;
import com.agentapi.rag.persistence.DocumentoChunkRepository;
import com.agentapi.rag.persistence.DocumentoEntity;
import com.agentapi.rag.persistence.DocumentoRepository;

@Service
public class RagSearchService {

    private final RagProperties ragProperties;
    private final JdbcTemplate jdbcTemplate;
    private final DocumentoChunkRepository chunkRepository;
    private final DocumentoRepository documentoRepository;
    private final boolean postgres;

    public RagSearchService(
            RagProperties ragProperties,
            JdbcTemplate jdbcTemplate,
            DocumentoChunkRepository chunkRepository,
            DocumentoRepository documentoRepository) {
        this.ragProperties = ragProperties;
        this.jdbcTemplate = jdbcTemplate;
        this.chunkRepository = chunkRepository;
        this.documentoRepository = documentoRepository;
        this.postgres = detectPostgres(jdbcTemplate);
    }

    public List<RagHit> search(String query) {
        return search(query, ragProperties.getTopK());
    }

    public List<RagHit> search(String query, int topK) {
        if (!ragProperties.isEnabled() || query == null || query.isBlank()) {
            return List.of();
        }
        String term = normalizeQuery(query);
        int limit = Math.max(1, topK);

        if (postgres) {
            List<RagHit> hits = searchPostgresWeb(term, limit);
            if (!hits.isEmpty()) {
                return hits;
            }
            hits = searchPostgresPlain(term, limit);
            if (!hits.isEmpty()) {
                return hits;
            }
            for (String alt : RagQueryVariants.fallbacks(term)) {
                hits = searchPostgresPlain(alt, limit);
                if (!hits.isEmpty()) {
                    return hits;
                }
                hits = searchPostgresIlike(alt, limit);
                if (!hits.isEmpty()) {
                    return hits;
                }
            }
            hits = searchPostgresIlike(term, limit);
            if (!hits.isEmpty()) {
                return hits;
            }
            return List.of();
        }
        return searchLike(term, limit);
    }

    private List<RagHit> searchPostgresIlike(String term, int limit) {
        String pattern = "%" + term.replace("%", "").replace("_", "") + "%";
        if (pattern.length() < 4) {
            return List.of();
        }
        String sql = """
                SELECT d.nm_titulo, d.nm_fonte, c.ds_conteudo, 1.0 AS score
                FROM agent.documento_chunk c
                INNER JOIN agent.documento d ON d.id_documento = c.id_documento
                WHERE c.ds_conteudo ILIKE ?
                ORDER BY c.nu_ordem ASC
                LIMIT ?
                """;
        return queryPostgres(sql, pattern, limit);
    }

    private List<RagHit> searchPostgresWeb(String term, int limit) {
        String sql = """
                SELECT d.nm_titulo, d.nm_fonte, c.ds_conteudo,
                       ts_rank(c.ds_busca, websearch_to_tsquery('portuguese', ?)) AS score
                FROM agent.documento_chunk c
                INNER JOIN agent.documento d ON d.id_documento = c.id_documento
                WHERE c.ds_busca @@ websearch_to_tsquery('portuguese', ?)
                ORDER BY score DESC
                LIMIT ?
                """;
        return queryPostgresFts(sql, term, limit);
    }

    private List<RagHit> searchPostgresPlain(String term, int limit) {
        String sql = """
                SELECT d.nm_titulo, d.nm_fonte, c.ds_conteudo,
                       ts_rank(c.ds_busca, plainto_tsquery('portuguese', ?)) AS score
                FROM agent.documento_chunk c
                INNER JOIN agent.documento d ON d.id_documento = c.id_documento
                WHERE c.ds_busca @@ plainto_tsquery('portuguese', ?)
                ORDER BY score DESC
                LIMIT ?
                """;
        return queryPostgresFts(sql, term, limit);
    }

    private List<RagHit> queryPostgresFts(String sql, String term, int limit) {
        return jdbcTemplate.query(
                sql,
                rowMapper(),
                term,
                term,
                limit);
    }

    private List<RagHit> queryPostgres(String sql, Object... args) {
        return jdbcTemplate.query(sql, rowMapper(), args);
    }

    private static RowMapper<RagHit> rowMapper() {
        return (rs, rowNum) -> new RagHit(
                rs.getString("nm_titulo"),
                rs.getString("nm_fonte"),
                rs.getString("ds_conteudo"),
                rs.getDouble("score"));
    }

    private List<RagHit> searchLike(String term, int limit) {
        String token = extractKeyword(term);
        List<DocumentoChunkEntity> chunks = chunkRepository.searchByContentLike(
                token,
                PageRequest.of(0, limit));
        List<RagHit> hits = new ArrayList<>();
        for (DocumentoChunkEntity chunk : chunks) {
            Optional<DocumentoEntity> doc = documentoRepository.findById(chunk.getIdDocumento());
            String titulo = doc.map(DocumentoEntity::getNmTitulo).orElse("Documento");
            String fonte = doc.map(DocumentoEntity::getNmFonte).orElse("desconhecida");
            hits.add(new RagHit(titulo, fonte, chunk.getDsConteudo(), 1.0));
        }
        return hits;
    }

    private static boolean detectPostgres(JdbcTemplate jdbcTemplate) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            return product != null && product.toLowerCase().contains("postgresql");
        } catch (Exception ex) {
            return false;
        }
    }

    private static String normalizeQuery(String query) {
        return query.trim().replaceAll("\\s+", " ");
    }

    /**
     * Fallback simples para LIKE quando FTS nao esta disponivel (ex.: H2 em testes).
     */
    private static String extractKeyword(String query) {
        if (query.length() <= 80) {
            return query;
        }
        return query.substring(0, 80);
    }
}
