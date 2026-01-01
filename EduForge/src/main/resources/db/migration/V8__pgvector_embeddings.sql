-- V8: Ajouter pgvector pour les embeddings sémantiques
-- Cette migration ajoute le support des embeddings vectoriels pour le RAG

-- Activer l'extension pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- Ajouter la colonne embedding à course_chunk (768 dimensions pour Gemini text-embedding-004)
ALTER TABLE course_chunk ADD COLUMN IF NOT EXISTS embedding vector(768);

-- Créer un index IVFFlat pour la recherche vectorielle rapide
-- L'index sera créé seulement après qu'il y aura des données
-- CREATE INDEX IF NOT EXISTS idx_chunk_embedding ON course_chunk USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Pour l'instant, on crée un index HNSW qui ne nécessite pas de données préexistantes
CREATE INDEX IF NOT EXISTS idx_chunk_embedding_hnsw ON course_chunk USING hnsw (embedding vector_cosine_ops);

-- Ajouter une colonne pour savoir si le chunk a été indexé avec embeddings
ALTER TABLE course_chunk ADD COLUMN IF NOT EXISTS has_embedding BOOLEAN DEFAULT FALSE;
