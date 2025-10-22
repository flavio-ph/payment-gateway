-- V1__create_tables.sql

-- Tabela de Usuários
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    data_criacao DATETIME NOT NULL
);

-- Tabela de Contas (Saldo)
CREATE TABLE conta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    saldo DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    versao BIGINT,
    data_atualizacao DATETIME,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Tabela de Cobranças
CREATE TABLE cobranca (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_origem_id BIGINT NOT NULL,
    cpf_destino VARCHAR(11) NOT NULL,
    valor DECIMAL(15, 2) NOT NULL,
    descricao VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    metodo_pagamento VARCHAR(50),
    data_criacao DATETIME NOT NULL,
    data_pagamento DATETIME,
    FOREIGN KEY (usuario_origem_id) REFERENCES usuario(id)
);

-- Tabela de Pagamentos (Registra a transação)
CREATE TABLE pagamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cobranca_id BIGINT NOT NULL,
    usuario_pagador_id BIGINT NOT NULL,
    valor DECIMAL(15, 2) NOT NULL,
    metodo VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    referencia_externa VARCHAR(255),
    data_criacao DATETIME NOT NULL,
    FOREIGN KEY (cobranca_id) REFERENCES cobranca(id),
    FOREIGN KEY (usuario_pagador_id) REFERENCES usuario(id)
);

-- Tabela de Depósitos (Registra a entrada de dinheiro)
CREATE TABLE deposito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    valor DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    referencia_externa VARCHAR(255) NOT NULL UNIQUE,
    data_criacao DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);