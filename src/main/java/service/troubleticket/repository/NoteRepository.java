package service.troubleticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import service.troubleticket.entity.NoteEntity;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long> {
}
