package com.example.demo.Service;

import com.example.demo.Controller.IfcController;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.shared.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.bimserver.interfaces.objects.SProject;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Service
public class ifcPostService {
    public static void postIfc(MultipartFile file, String schema, Long parentPoid){
        try {
            String relativeFolder = "src\\main\\resources\\BimServerInstallTempFolder\\";
            UUID uniqueId = UUID.randomUUID();
            String uniqueName = file.getName()+ "-"+ uniqueId;

            SProject newProject = IfcController.client.getServiceInterface().addProjectAsSubProject(uniqueName, parentPoid,schema);
            long poid = newProject.getOid();
            File fileOfSubject = new File(relativeFolder + uniqueName + ".ifc");
            try {
                file.transferTo(fileOfSubject.getAbsoluteFile());
            } catch (IOException e) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            // This method is an easy way to find a compatible deserializer for the combination of the "ifc" file extension and this project. You can also get a specific deserializer if you want to.
            SDeserializerPluginConfiguration deserializer = IfcController.client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", poid);

            // Make sure you change this to a path to a local IFC file
            Path demoIfcFile = Paths.get(relativeFolder+uniqueName +".ifc");

            // Here we actually checkin the IFC file. Flow.SYNC indicates that we only want to continue the code-flow after the checkin has been completed
            IfcController.client.checkinSync(poid,"",deserializer.getOid(),false,demoIfcFile);
            Files.delete(fileOfSubject.toPath());
        } catch (ServiceException | PublicInterfaceNotFoundException | IOException e) {
            ResponseEntity.internalServerError();
        }
    }

    public static void deleteProject(Long oid){

        try {
            IfcController.client.getServiceInterface().deleteProject(oid);
        } catch (ServerException e) {
            ResponseEntity.internalServerError();
        } catch (UserException e) {
            ResponseEntity.badRequest();
        }
    }
}
