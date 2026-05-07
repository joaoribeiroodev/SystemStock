USE estoque_db;


CREATE TABLE users (
    id               INT           AUTO_INCREMENT PRIMARY KEY,


    username         VARCHAR(100)  NOT NULL UNIQUE,
    psw       VARCHAR(255)  NOT NULL,

    nome             VARCHAR(100),
    sobreNome        VARCHAR(100),
    matricula        VARCHAR(50)   UNIQUE,
    cpf              VARCHAR(14)   UNIQUE,
    sexo             ENUM('Masculino', 'Feminino', 'Outro', 'Prefiro não informar'),
    data_nascimento  DATE,
    email            VARCHAR(150)  UNIQUE,
    telefone         VARCHAR(20),
    funcao           VARCHAR(100),
    
     -- endereço
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
    id               INT            AUTO_INCREMENT PRIMARY KEY,

    codigo_barras    VARCHAR(100)   NOT NULL UNIQUE,
    nome_produto     VARCHAR(255)   NOT NULL,
    fabricante       VARCHAR(255),
    marca            VARCHAR(255),
    data_fabricacao  DATE,
    data_vencimento  DATE,

    quantidade       BIGINT         NOT NULL DEFAULT 0 CHECK (quantidade >= 0),

    valor            DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total            DECIMAL(10, 2) NOT NULL DEFAULT 0.00,

    status           ENUM('ENTRADA', 'SAIDA') NOT NULL,

    criado_em        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_nome_produto (nome_produto)
);

CREATE TABLE movimentacoes (
    id                  INT      AUTO_INCREMENT PRIMARY KEY,
    produto_id          INT      NOT NULL,
    tipo                ENUM('ENTRADA', 'SAIDA') NOT NULL,
    quantidade          BIGINT   NOT NULL CHECK (quantidade > 0),

    data_movimentacao   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movimentacao_produto
        FOREIGN KEY (produto_id)
        REFERENCES produtos (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    -- ✅ ADICIONADO: índice composto para acelerar a query do gráfico
    --    (filtra por produto_id, extrai MONTH e YEAR de data_movimentacao)
    INDEX idx_movimentacao_produto_data (produto_id, data_movimentacao)
);