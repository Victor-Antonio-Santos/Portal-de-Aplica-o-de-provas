USE provas_online;

INSERT INTO usuario (nome, cpf, email, senha, perfil, status, aplicador_responsavel_id, turma_id, data_cadastro)
VALUES
('Administrador Master', '52998224725', 'admin@empresa.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ROLE_ADMIN', 'ATIVO', NULL, NULL, NOW());

INSERT INTO turma (nome, status, aplicador_responsavel_id, data_cadastro)
VALUES ('Turma Master', 'ATIVA', 1, NOW());

INSERT INTO usuario (nome, cpf, email, senha, perfil, status, aplicador_responsavel_id, turma_id, data_cadastro)
VALUES
('Colaborador Teste', '11144477735', 'colaborador@empresa.com', '07236c90ef9685aae0f2b1107dbf0df0a3745c2d09e47936e572d9cd6452a3a6', 'ROLE_COLABORADOR', 'ATIVO', 1, 1, NOW());

INSERT INTO prova (titulo, descricao, tempo_minutos, nota_minima, status, data_criacao, mostrar_resultado)
VALUES ('Integracao Corporativa', 'Prova inicial para novos colaboradores.', 30, 7.00, 'ATIVA', NOW(), 1);

INSERT INTO questao (prova_id, enunciado, tipo, peso, ordem_exibicao, ativo)
VALUES
(1, 'A empresa valoriza comportamento etico no ambiente de trabalho?', 'VERDADEIRO_FALSO', 2.00, 1, 1),
(1, 'Qual atitude e esperada durante a integracao?', 'ESCOLHA_UNICA', 3.00, 2, 1);

INSERT INTO alternativa (questao_id, texto, correta)
VALUES
(1, 'Verdadeiro', 1),
(1, 'Falso', 0),
(2, 'Seguir procedimentos e participar dos treinamentos.', 1),
(2, 'Ignorar as orientacoes iniciais.', 0),
(2, 'Compartilhar credenciais com colegas.', 0);
