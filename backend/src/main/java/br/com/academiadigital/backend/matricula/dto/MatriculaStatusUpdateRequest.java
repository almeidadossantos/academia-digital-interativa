package br.com.academiadigital.backend.matricula.dto;

import br.com.academiadigital.backend.matricula.StatusMatricula;
import jakarta.validation.constraints.NotNull;

public class MatriculaStatusUpdateRequest {

    @NotNull(
            message = "O status da matrícula é obrigatório."
    )
    private StatusMatricula status;

    public StatusMatricula getStatus() {
        return status;
    }

    public void setStatus(StatusMatricula status) {
        this.status = status;
    }
}