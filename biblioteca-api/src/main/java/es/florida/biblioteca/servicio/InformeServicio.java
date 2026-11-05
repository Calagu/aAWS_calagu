package es.florida.biblioteca.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import es.florida.biblioteca.dao.InformeDao;
import es.florida.biblioteca.excepcion.DatosInvalidosException;
import es.florida.biblioteca.modelo.LibroMasPrestado;

@Service
public class InformeServicio {

    public static final int LIMITE_MINIMO = 1;
    public static final int LIMITE_MAXIMO = 20;

    private final InformeDao informeDao;

    public InformeServicio(InformeDao informeDao) {
        this.informeDao = informeDao;
    }

    public List<LibroMasPrestado> masPrestados(int limite) {
        if (limite < LIMITE_MINIMO || limite > LIMITE_MAXIMO) {
            throw new DatosInvalidosException("El parámetro limite debe estar entre "
                    + LIMITE_MINIMO + " y " + LIMITE_MAXIMO);
        }
        return informeDao.masPrestados(limite);
    }
}
