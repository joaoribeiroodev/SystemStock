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

CREATE TABLE produtos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigos_barras VARCHAR(100) NOT NULL,
    nome_produto VARCHAR(255) NOT NULL,
    fabricante VARCHAR(255),
    marca VARCHAR(255),
    data_fabricacao DATE,
    data_vencimento DATA,
    quantidade BIGINT,
    valor DECIMAL(10,2),
    total DECIMAL(10,2),
);

INSERT INTO users (
    username, psw, nameFirst, sobreNome, matricula, cpf, sexo, data_nascimento,
    email, telefone, funcao,
    cep, endereco, bairro, cidade, estado, numero, complemento
) VALUES (
    'admin', '1234',
    'Joao','Silva', '2023001', '123.456.789-00', 'Masculino', '2000-05-10',
    'joao@email.com', '71999999999', 'Administrador',
    '40000-000', 'Rua das Flores', 'Centro', 'Salvador', 'BA', '123', 'Apto 101'
);
