package es.iesjandula.ReaktorIssuesServer.rest;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.ReaktorIssuesServer.dto.CrearIncidenciaDTO;
import es.iesjandula.ReaktorIssuesServer.dto.FiltroBusqueda;
import es.iesjandula.ReaktorIssuesServer.dto.IncidenciaDTO;
import es.iesjandula.ReaktorIssuesServer.entity.IncidenciaEntity;
import es.iesjandula.ReaktorIssuesServer.mappers.IncidenciaMapper;
import es.iesjandula.ReaktorIssuesServer.repository.IIncidenciaRepository;
import es.iesjandula.ReaktorIssuesServer.utils.Constants;
import es.iesjandula.ReaktorIssuesServer.utils.IssuesServerError;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para gestionar incidencias en el sistema.
 * 
 * Esta clase proporciona endpoints para crear, actualizar, buscar y eliminar
 * incidencias en la base de datos. Utiliza un repositorio para interactuar con
 * los datos de las incidencias y un mapeador para convertir entre objetos DTO y
 * entidades de base de datos.
 * 
 * Los métodos de esta clase devuelven respuestas adecuadas basadas en el
 * resultado de las operaciones, incluyendo códigos de estado HTTP para informar
 * sobre el éxito o fracaso de las solicitudes.
 * 
 * Las operaciones que se pueden realizar incluyen:
 * <ul>
 * <li><strong>Crear Incidencia:</strong> Permite la creación de nuevas
 * incidencias.</li>
 * <li><strong>Actualizar Incidencia:</strong> Permite la actualización de
 * incidencias existentes.</li>
 * <li><strong>Buscar Incidencias:</strong> Permite buscar incidencias basadas
 * en criterios específicos.</li>
 * <li><strong>Eliminar Incidencia:</strong> Permite la eliminación de
 * incidencias existentes.</li>
 * </ul>
 * 
 * Se requiere que los encabezados y los cuerpos de las solicitudes contengan
 * información válida para que las operaciones se ejecuten correctamente. En
 * caso de errores, se devuelven mensajes informativos y códigos de estado HTTP
 * adecuados.
 * 
 * @see FiltroBusqueda
 * @see IncidenciaDTO
 * @see IncidenciaEntity
 * @see IIncidenciaRepository
 * @see IncidenciaMapper
 */
@Slf4j 
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/incidencias")
public class IncidenciaController
{

	@Autowired
	// Auto-inyeccion de repositorio.
	private IIncidenciaRepository iIncidenciaRepository;

	@Autowired
	// Auto-inyeccion de mapeador de dto-entidad.
	IncidenciaMapper incidenciaMapper;

	/**
	 * Crear o actualizar una incidencia en el sistema.
	 * 
	 * Este método recibe un DTO que contiene la información de la incidencia a
	 * crear o actualizar y un encabezado que incluye el correo del docente. Se realiza la
	 * validación de los parámetros recibidos y, si son válidos, se crea una nueva
	 * incidencia o se actualiza en la base de datos. Si los datos no son válidos, se devuelve un
	 * código de estado HTTP 400 (Bad Request). En caso de un error inesperado, se
	 * devuelve un código de estado HTTP 500 (Internal Server Error).
	 * 
	 * @param correoDocente      El correo electrónico del docente, que se espera en
	 *                           el encabezado de la solicitud. Este parámetro es
	 *                           requerido y no puede ser nulo.
	 * @param incidenciaDTO El objeto DTO que contiene la información de la
	 *                           incidencia a crear o actualizar. Este parámetro es requerido y
	 *                           no puede ser nulo.
	 * @return ResponseEntity<String> La respuesta que indica el resultado de la
	 *         operación. Si la creación o actualización es exitosa, se devuelve un código de estado
	 *         201 (Created) junto con un mensaje de éxito en caso de haber sido creada o 200(OK) 
	 *         en caso de ser actualizada.Si hay un error de validación, se devuelve 
	 *         un código de estado 400 (Bad Request) con un mensaje de error. 
	 *         Si ocurre un error inesperado, se devuelve un
	 *         código de estado 500 (Internal Server Error) con un mensaje de error.
	 */
	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<?> crearActualizarIncidencia(
			@RequestHeader(value = "correo-docente", required = true) String correoDocente,
			@RequestBody(required = true) IncidenciaDTO incidenciaDTO)
	{
		try
		{
			// Mensaje informativo a devolver
			ResponseEntity<String> response = null;
			
			// Loguea los parametros recibidos para fines diagnosticos.
			log.debug("Parametros recibidos:\n" + incidenciaDTO.toString());
				
			// Si el numero de aula está vacio o solo espacios.
			if (incidenciaDTO.getNumeroAula() == null || incidenciaDTO.getNumeroAula().isBlank())
			{
				log.error("Intento de creación de incidencia con numero de aula no definido");
				return ResponseEntity.badRequest().body("ERROR: Numero de aula nulo o vacio.");
			}

			// Si la descripcion está vacia o solo espacios.
			if (incidenciaDTO.getDescripcionIncidencia() == null
					|| incidenciaDTO.getDescripcionIncidencia().isBlank())
			{
				log.error("Intento de creación de incidencia con descripcion no definida o menor de 15 caracteres");
				return ResponseEntity.badRequest().body("ERROR: Descripcion de incidencia nulo, vacio o menor de 15 caracteres.");
			}
			
			// Si tanto numero de aula como descripción han sido definidos correctamente
			// creamos nueva incidencia.
			IncidenciaEntity incidencia = new IncidenciaEntity();
			
			
			// Si no existe la incidencia
			if(!iIncidenciaRepository.existsByCompositeId(incidenciaDTO.getNumeroAula(), 
			incidenciaDTO.getCorreoDocente(), incidenciaDTO.getFechaIncidencia()))
			{
				// Objeto fecha de hoy
				Date today = new Date();
				
				// Primer parametro  - Numero de Aula
				// Segundo parametro - Correo del Docente
				// Tercer parametro  - Fecha Actual
				// Cuarto parametro  - Descripcion
				// Quinto parametro  - Estado(Pendiente)
				// Sexto parametro   - Comentario(Vacío)
				incidencia = new IncidenciaEntity(
						incidenciaDTO.getNumeroAula(), 
						correoDocente, 
						today,
						incidenciaDTO.getDescripcionIncidencia(),
						Constants.ESTADO_PENDIENTE,
						"");
			
				// Información para indicar la inicializacion de la incidencia
				log.debug("DEBUG: Objeto incidencia inicializado correctamente:\n " + incidencia.toString());

				// Informe de incidencia creada con exito
				response = ResponseEntity.status(HttpStatus.CREATED).body("EXITO: Incidencia creada con exito");
			}
			else
			{
				// Mapear la incidencia
				incidencia = incidenciaMapper.mapToEntity(incidenciaDTO);
				
				// Información para indicar la inicializacion de la incidencia
				log.debug("DEBUG: Objeto incidencia inicializado correctamente:\n " + incidencia.toString());
				
				// Informe de incidencia actualizada con exito
				response = ResponseEntity.status(HttpStatus.OK).body("EXITO: Incidencia actualizada con exito");
			}
			
			// Finalmente guarda la incidencia en la BBDD.
			iIncidenciaRepository.saveAndFlush(incidencia);

			// Información para registro.
			log.info("INFO: El objeto guardado en base de datos es:\n" + incidencia.toString());

			// Informe a cliente del exito de la operacion.
			return response;

		}
		catch (Exception createIssueException)
		{
			String message = "Excepción capturada en crearIncidencia(): {}" + createIssueException.getMessage();
			log.error(message, createIssueException);
	        IssuesServerError serverError = new IssuesServerError(0, message, createIssueException);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serverError.getMapError());
		}
	}

	/**
	 * Maneja las solicitudes GET para buscar incidencias en base a los criterios
	 * proporcionados en el filtro de búsqueda.
	 * 
	 * Este método recibe un objeto {@link FiltroBusqueda} que contiene los
	 * criterios de búsqueda para filtrar las incidencias almacenadas. Realiza la
	 * búsqueda utilizando el repositorio correspondiente y devuelve una lista de
	 * incidencias que cumplen con los criterios. En caso de que no se encuentren
	 * incidencias, se devuelve un mensaje informativo con un código de estado 404
	 * (Not Found). Si ocurre algún error durante el proceso, se devuelve un mensaje
	 * de error con un código de estado 500 (Internal Server Error).
	 *
	 * @param filtro El objeto {@link FiltroBusqueda} que contiene los criterios de
	 *          búsqueda para filtrar las incidencias.
	 * @return Un objeto {@link ResponseEntity} que puede contener:
	 *         <ul>
	 *         <li>Una lista de {@link IncidenciaDTO} en caso de que se encuentren
	 *         incidencias, con código de estado 200 (OK).</li>
	 *         <li>Un mensaje de error si no se encuentran incidencias, con código
	 *         de estado 404 (Not Found).</li>
	 *         <li>Un mensaje de error general, en caso de excepciones inesperadas,
	 *         con código de estado 500 (Internal Server Error).</li>
	 *         </ul>
	 */
	@PostMapping("/crear_incidencia")
	public ResponseEntity<?> crearIncidencia(@RequestBody CrearIncidenciaDTO crearIncidenciaDTO) 
	{
	    try 
	    {
	        // Loguea los parámetros recibidos
	        log.debug("DEBUG: Parametros de incidencia recibidos:\n {}", crearIncidenciaDTO.toString());

	        // Validar que los datos obligatorios estén presentes
	        if (crearIncidenciaDTO.getNumeroAula() == null || crearIncidenciaDTO.getNumeroAula().isEmpty()) 
	        {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El número de aula es obligatorio.");
	        }

	        if (crearIncidenciaDTO.getCorreoDocente() == null || crearIncidenciaDTO.getCorreoDocente().isEmpty())
	        {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo del docente es obligatorio.");
	        }

	        if (crearIncidenciaDTO.getDescripcionIncidencia() == null || crearIncidenciaDTO.getDescripcionIncidencia().isEmpty()) 
	        {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La descripción de la incidencia es obligatoria.");
	        }
	        

	        // Formatear y asignar fecha si no se proporciona
	        Date fechaIncidenciaF;
	        if (crearIncidenciaDTO.getFechaIncidencia() == null)
	        {
	            fechaIncidenciaF = new Date(); // Fecha actual
	            crearIncidenciaDTO.setFechaIncidencia(fechaIncidenciaF);
	        } 
	        else 
	        {
	            fechaIncidenciaF = crearIncidenciaDTO.getFechaIncidencia();
	        }
	        

	        // Crear un nuevo objeto entidad para guardar en la base de datos
	        IncidenciaEntity nuevaIncidencia = new IncidenciaEntity();
	        nuevaIncidencia.setNumeroAula(crearIncidenciaDTO.getNumeroAula());
	        nuevaIncidencia.setCorreoDocente(crearIncidenciaDTO.getCorreoDocente());
	        nuevaIncidencia.setFechaIncidencia(fechaIncidenciaF);
	        nuevaIncidencia.setDescripcionIncidencia(crearIncidenciaDTO.getDescripcionIncidencia());
	        
	        // Guardar la incidencia en la base de datos
	        iIncidenciaRepository.saveAndFlush(nuevaIncidencia);

	        // Loguea el éxito de la operación
	        log.info("Incidencia creada con éxito: {}", nuevaIncidencia);

	        // Devuelve la respuesta exitosa
	        return ResponseEntity.status(HttpStatus.CREATED).body("Incidencia creada con éxito.");
	    } 
	    catch (Exception ex) 
	    {
	        String message = "ERROR: Error al crear la incidencia:\n " + ex.getMessage();
	        log.error(message, ex);

	        IssuesServerError serverError = new IssuesServerError(3, message, ex);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serverError.getMapError());
	    }
	}
	

	/**
	 * Elimina una incidencia de la base de datos basándose en los detalles
	 * proporcionados en el DTO. Verifica primero si la incidencia existe, y si no,
	 * retorna un código de estado 404 (NOT_FOUND). Si la incidencia existe, la
	 * elimina y retorna un código de estado 204 (NO_CONTENT) indicando éxito.
	 *
	 * @param dto El objeto {@link IncidenciaDTO} que contiene los detalles de la
	 *            incidencia a eliminar. Los campos {@code numeroAula},
	 *            {@code correoDocente} y {@code fechaIncidencia} son obligatorios.
	 * @return {@link ResponseEntity} con el código de estado correspondiente: - 204
	 *         (NO_CONTENT) si la incidencia fue eliminada correctamente. - 404
	 *         (NOT_FOUND) si la incidencia no fue encontrada en la base de datos. -
	 *         400 (BAD_REQUEST) si los parámetros del DTO no son válidos. - 500
	 *         (INTERNAL_SERVER_ERROR) en caso de errores inesperados.
	 * @throws IllegalArgumentException si los parámetros del DTO son inválidos.
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	public ResponseEntity<?> borraIncidencia(@RequestBody(required = true) IncidenciaDTO dto)
	{
		try
		{
			// Mapea el DTO recibido a la entidad de Incidencia y controla parametros NULL.
			IncidenciaEntity inEntity = incidenciaMapper.mapToEntity(dto);

			// Verifica si la incidencia existe en la base de datos.
			if (!(iIncidenciaRepository.existsByCompositeId(inEntity.getNumeroAula(), inEntity.getCorreoDocente(),
					inEntity.getFechaIncidencia())))
			{
				// Si no existe la incidencia, responde con 404.
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incidencia no encontrada.");
			}

			// Elimina la incidencia de la base de datos y loguea la accion.
			iIncidenciaRepository.delete(inEntity);
			log.info("INFO: Incidencia eliminada con exito.\n{}", inEntity.toString());

			// Respuesta HTTP de objeto borrado con exito.
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("INFO:Incidencia eliminada con exito.");

		}
		// Error en parametros DTO u objeto nulo.
		catch (IllegalArgumentException illegalArgumentException)
		{
			String message = "ERROR: Error en parametros del objeto recibido en borraIncidencia().\n{}" + illegalArgumentException.getMessage();
			log.error(message, illegalArgumentException);
			IssuesServerError serverError = new IssuesServerError(1, message, illegalArgumentException);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(serverError.getMapError());
		}
		// Captura de errores no esperados o calculados.
		catch (Exception deleteIssueException)
		{
			String message = "Error inesperado en borraIncidencia() .\nMensaje de error: " + deleteIssueException.getMessage();
			log.error(message, deleteIssueException);
			IssuesServerError serverError = new IssuesServerError(4, message, deleteIssueException);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serverError.getMapError());
		}
	}
}