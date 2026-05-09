-- Alignement blog pour le projet JavaFX (base voyage-1)

-- 1) Workflow publication blog
ALTER TABLE blog
  MODIFY status TINYINT(1) DEFAULT 0,
  MODIFY publication_requested TINYINT(1) DEFAULT 0;

-- 2) Index utiles dashboard blog/admin
CREATE INDEX IF NOT EXISTS idx_blog_status ON blog(status);
CREATE INDEX IF NOT EXISTS idx_blog_publication_requested ON blog(publication_requested);
CREATE INDEX IF NOT EXISTS idx_comment_blog ON commentaire(blog_id);
CREATE INDEX IF NOT EXISTS idx_comment_report_comment ON comment_report(commentaire_id);
CREATE INDEX IF NOT EXISTS idx_comment_report_status ON comment_report(status);

-- 3) Tables auxiliaires du dashboard modération (créées aussi automatiquement par le code)
CREATE TABLE IF NOT EXISTS moderation_analysis (
  report_id BIGINT PRIMARY KEY,
  toxicity_score INT NOT NULL,
  explicit_threat BOOLEAN NOT NULL DEFAULT FALSE,
  categories TEXT,
  sensitive_words TEXT,
  language VARCHAR(16) NOT NULL DEFAULT 'fr',
  confidence DOUBLE NOT NULL DEFAULT 0.5,
  recommendation VARCHAR(16) NOT NULL DEFAULT 'APPROVE',
  justification TEXT,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS moderation_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT NOT NULL,
  actor VARCHAR(128) NOT NULL,
  action VARCHAR(64) NOT NULL,
  details TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
