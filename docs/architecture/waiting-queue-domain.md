# Waiting Queue ⏳

## Objetivo
Organizar a ordem de entrada das equipes nas próximas partidas, respeitando as regras de prioridade estabelecidas pela sessão.

## Responsabilidades
- manter a ordem das equipes que aguardam jogar;
- inserir novas equipes respeitando prioridades;
- fornecer a próxima equipe disponível;
- reorganizar a fila quando necessário;
- impedir estados inválidos.

## Regras de negócio
RN01
> Equipes compostas por jogadores que ainda não participaram da sessão possuem prioridade sobre equipes que já participaram.

RN02
> Entre equipes que nunca jogaram, deve ser respeitada a ordem cronológica de criação.

RN03
> Equipes que já participaram retornam ao final da fila.

RN04
> Uma equipe nunca pode aparecer duas vezes na fila.

RN05
> A fila nunca contém equipes que estejam disputando a partida atual.

## Estados inválidos
- Equipe duplicada.
- Equipe sem jogadores.
- Equipe da partida atual presente na fila.
- Ordem de prioridade violada.

## Dúvidas arquiteturais
- A WaitingQueue deve conhecer a Match atual ou isso é responsabilidade da Session?
- A WaitingQueue deve conhecer apenas os times da fila ou todos os times da sessão?
- A WaitingQueue deveria conhecer regras de prioridade ou apenas executá-las?