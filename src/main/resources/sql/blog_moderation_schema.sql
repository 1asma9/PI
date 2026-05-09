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
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (report_id) REFERENCES comment_report(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS moderation_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT NOT NULL,
  actor VARCHAR(128) NOT NULL,
  action VARCHAR(64) NOT NULL,
  details TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (report_id) REFERENCES comment_report(id) ON DELETE CASCADE
);
