package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, UUID> {

}
