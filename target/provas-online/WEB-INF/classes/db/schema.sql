CREATE DATABASE IF NOT EXISTS provas_online CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE provas_online;

CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(120),
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    aplicador_responsavel_id BIGINT,
    turma_id BIGINT,
    data_cadastro DATETIME NOT NULL,
    CONSTRAINT fk_usuario_aplicador_responsavel FOREIGN KEY (aplicador_responsavel_id) REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS turma (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    aplicador_responsavel_id BIGINT NOT NULL,
    data_cadastro DATETIME NOT NULL,
    CONSTRAINT fk_turma_aplicador_responsavel FOREIGN KEY (aplicador_responsavel_id) REFERENCES usuario(id)
);

ALTER TABLE usuario
    ADD CONSTRAINT fk_usuario_turma FOREIGN KEY (turma_id) REFERENCES turma(id);

CREATE TABLE IF NOT EXISTS prova (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(160) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    tempo_minutos INT NOT NULL,
    nota_minima DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_criacao DATETIME NOT NULL,
    mostrar_resultado BIT NOT NULL
);

CREATE TABLE IF NOT EXISTS questao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prova_id BIGINT NOT NULL,
    enunciado VARCHAR(1000) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    peso DECIMAL(10,2) NOT NULL,
    ordem_exibicao INT NOT NULL,
    ativo BIT NOT NULL,
    CONSTRAINT fk_questao_prova FOREIGN KEY (prova_id) REFERENCES prova(id)
);

CREATE TABLE IF NOT EXISTS alternativa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    questao_id BIGINT NOT NULL,
    texto VARCHAR(500) NOT NULL,
    correta BIT NOT NULL,
    CONSTRAINT fk_alternativa_questao FOREIGN KEY (questao_id) REFERENCES questao(id)
);

CREATE TABLE IF NOT EXISTS prova_colaborador (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prova_id BIGINT NOT NULL,
    colaborador_id BIGINT NOT NULL,
    data_envio DATETIME NOT NULL,
    data_inicio DATETIME,
    data_fim DATETIME,
    data_limite DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    nota DECIMAL(10,2),
    tentativa INT NOT NULL,
    tentativas_permitidas INT NOT NULL,
    tempo_gasto INT,
    disponivel_imediatamente BIT NOT NULL,
    CONSTRAINT fk_pc_prova FOREIGN KEY (prova_id) REFERENCES prova(id),
    CONSTRAINT fk_pc_colaborador FOREIGN KEY (colaborador_id) REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS resposta_colaborador (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prova_colaborador_id BIGINT NOT NULL,
    questao_id BIGINT NOT NULL,
    alternativa_id BIGINT NOT NULL,
    CONSTRAINT fk_resposta_pc FOREIGN KEY (prova_colaborador_id) REFERENCES prova_colaborador(id),
    CONSTRAINT fk_resposta_questao FOREIGN KEY (questao_id) REFERENCES questao(id),
    CONSTRAINT fk_resposta_alternativa FOREIGN KEY (alternativa_id) REFERENCES alternativa(id)
);

CREATE TABLE IF NOT EXISTS log_operacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    acao VARCHAR(120) NOT NULL,
    usuario VARCHAR(160),
    descricao VARCHAR(500) NOT NULL,
    data_hora DATETIME NOT NULL
);
