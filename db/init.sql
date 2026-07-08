USE estoque_db;


CREATE TABLE users (
    id               INT           AUTO_INCREMENT PRIMARY KEY,

    username         VARCHAR(100)  NOT NULL UNIQUE COLLATE utf8mb4_bin,
    psw              VARCHAR(255)  NOT NULL,

    nameFirst        VARCHAR(100),
    sobreNome        VARCHAR(100),
    matricula        VARCHAR(50)   UNIQUE,
    cpf              VARCHAR(14)   UNIQUE,
    sexo             ENUM('Masculino', 'Feminino'),
    dtaNascimento    DATE,
    email            VARCHAR(150)  UNIQUE,
    telefone         VARCHAR(20),
    funcao           VARCHAR(100),

    cep              VARCHAR(10),
    endereco         VARCHAR(150),
    bairro           VARCHAR(100),
    cidade           VARCHAR(100),
    estado           CHAR(2),
    numero           VARCHAR(10),
    complemento      VARCHAR(100),

    criado_em        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE produtos (
    id                INT            AUTO_INCREMENT PRIMARY KEY,

    codigo_barras     VARCHAR(100)   NOT NULL UNIQUE,
    nome_produto      VARCHAR(255)   NOT NULL,
    fabricante        VARCHAR(255),
    marca             VARCHAR(255),
    data_fabricacao   DATE,
    data_vencimento   DATE,

    quantidade        BIGINT         NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    quantidade_minima BIGINT         NOT NULL DEFAULT 1 CHECK (quantidade_minima > 0),

    valor             DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total             DECIMAL(10, 2) NOT NULL DEFAULT 0.00,

    prateleira           VARCHAR(50)   NULL,
    local_armazenamento  VARCHAR(100)  NULL,

    status            ENUM('ENTRADA', 'SAIDA') NOT NULL,
    ativo             BOOLEAN        NOT NULL DEFAULT TRUE,

    criado_em         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_nome_produto (nome_produto)
);

CREATE TABLE movimentacoes (
    id                  INT       AUTO_INCREMENT PRIMARY KEY,
    produto_id          INT       NOT NULL,
    tipo                ENUM('ENTRADA', 'SAIDA') NOT NULL,
    quantidade          BIGINT    NOT NULL CHECK (quantidade > 0),

    data_movimentacao   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movimentacao_produto
        FOREIGN KEY (produto_id)
        REFERENCES produtos (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_movimentacao_produto_data (produto_id, data_movimentacao)
);

CREATE TABLE solicitacoes_compra (
    id                    INT       AUTO_INCREMENT PRIMARY KEY,
    produto_id            INT       NOT NULL,
    
    codigo_barras         VARCHAR(100)  NOT NULL,
    nome_produto          VARCHAR(255)  NOT NULL,

    quantidade_atual      BIGINT    NOT NULL,
    quantidade_minima     BIGINT    NOT NULL,
    quantidade_sugerida   BIGINT    NOT NULL,

    status                ENUM('PENDENTE', 'EM_ANDAMENTO', 'ATENDIDA', 'CANCELADA')
                              NOT NULL DEFAULT 'PENDENTE',
    observacao            VARCHAR(255) NULL,

    criado_em             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_solicitacao_produto
        FOREIGN KEY (produto_id)
        REFERENCES produtos (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_solicitacao_produto_status (produto_id, status)
);

INSERT INTO users (username, psw, funcao)
VALUES ('hugo', 
'$2a$10$zdrY5PUKObc4.N8okesIyOxJDG6VtwNr64QJglbLiEGZzAXtf7qPS',
'ADMIN');


