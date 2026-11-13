package ma.esith.stockims.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ImportExceptionAdvice {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String fichierTropVolumineux(RedirectAttributes attributs) {
        attributs.addFlashAttribute("erreur", "Import impossible : le fichier ne doit pas dépasser 5 Mo.");
        return "redirect:/articles";
    }
}
