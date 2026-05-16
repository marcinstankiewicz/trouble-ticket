package service.troubleticket.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import service.troubleticket.persistence.entity.NoteEntity;
import service.troubleticket.rs.v1.dto.NoteResponse;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    @Mapping(target="date", source = "createdAt")
    NoteResponse mapNoteToResponse(NoteEntity entity);
}
