use estoque_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,

    -- login
    username VARCHAR(100) NOT NULL,
    psw VARCHAR(100) NOT NULL,

    -- dados pessoais
    nameFirst VARCHAR(100),
    sobreNome VARCHAR(100),
    matricula VARCHAR(50) UNIQUE,
    cpf VARCHAR(14) UNIQUE,
    sexo ENUM('Masculino', 'Feminino'),
    data_nascimento DATE,
    email VARCHAR(150) UNIQUE,
    telefone VARCHAR(20),
    funcao VARCHAR(100),

    -- endereço
    cep VARCHAR(10),
    endereco VARCHAR(150),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(50),
    numero VARCHAR(10),
    complemento VARCHAR(100)
);

create table produtos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_barras VARCHAR(100) NOT NULL,
    nome_produto VARCHAR(255) NOT NULL,
    fabricante VARCHAR(255),
    marca VARCHAR(255),
    data_fabricacao DATE,
    data_vencimento DATE,
    quantidade BIGINT,
    valor DECIMAL(10,2),
    total DECIMAL(10,2),
    status VARCHAR(255)
);

CREATE TABLE movimentacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    produto_id INT,
    tipo ENUM('ENTRADA', 'SAIDA'),
    quantidade BIGINT,
    data_movimentacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
);

