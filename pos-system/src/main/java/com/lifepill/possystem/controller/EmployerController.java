package com.lifepill.possystem.controller;

import com.lifepill.possystem.dto.EmployerBankDetailsDTO;
import com.lifepill.possystem.dto.EmployerDTO;
import com.lifepill.possystem.dto.EmployerWithBankDTO;
import com.lifepill.possystem.dto.EmployerWithoutImageDTO;
import com.lifepill.possystem.dto.requestDTO.EmployerUpdate.*;
import com.lifepill.possystem.entity.EmployerBankDetails;
import com.lifepill.possystem.exception.NotFoundException;
import com.lifepill.possystem.service.EmployerService;
import com.lifepill.possystem.util.StandardResponse;
import com.lifepill.possystem.util.mappers.EmployerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controller class for managing employer-related operations.
 */
@RestController
@RequestMapping("lifepill/v1/employers")
public class EmployerController {

    @Autowired
    private EmployerService employerService;

    @Autowired
    private EmployerMapper employerMapper;

    public static String uploadDirectory = System.getProperty("user.dir") + "/uploads";

    /**
     * Saves an employer without an image.
     *
     * @param cashierWithoutImageDTO DTO containing details of the employer without image.
     * @return A string indicating the success of the operation.
     */
    @PostMapping("/save-without-image")
    public String saveCashierWithoutImage(@RequestBody EmployerWithoutImageDTO cashierWithoutImageDTO) {
        employerService.saveEmployerWithoutImage(cashierWithoutImageDTO);
        return "saved";
    }

    /**
     * Saves an employer with an image.
     *
     * @param employerDTO DTO containing details of the employer including image.
     * @param file        MultipartFile representing the image file.
     * @return A string indicating the success of the operation.
     * @throws IOException If an I/O error occurs.
     */
    @PostMapping("/save-with-image")
    public String saveEmployerWithImage(
            @ModelAttribute EmployerDTO employerDTO,
            @RequestParam("file") MultipartFile file
    )
            throws IOException {
        // Check if a file is provided
        if (!file.isEmpty()) {
            // Convert MultipartFile to byte array
            byte[] profileImage = file.getBytes();
            // Set the profile image in the employerDTO
            employerDTO.setProfileImage(profileImage);
        }
        // Save the cashier along with the profile photo
        employerService.saveEmployer(employerDTO);
        return "saved";
    }

    /**
     * Retrieves the profile photo of an employer by ID.
     *
     * @param employerId The ID of the employer.
     * @return ResponseEntity containing the profile photo byte array.
     */
    @GetMapping("/profile-photo/{employerId}")
    @Transactional
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable int employerId) {
        // Retrieve the cashier entity by ID
        EmployerDTO cashier = employerService.getEmployerById(employerId);

        // Check if the cashier exists
        if (cashier != null && cashier.getProfileImage() != null) {
            // Return the profile image byte array with appropriate headers
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG) // Adjust content type based on image format
                    .body(cashier.getProfileImage());
        } else {
            // If the cashier or profile photo doesn't exist, return a not found response
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates details of an employer.
     *
     * @param employerId                 The ID of the employer to be updated.
     * @param cashierAllDetailsUpdateDTO DTO containing updated details of the employer.
     * @return A string indicating the success of the operation.
     */
    @PutMapping("/update/{employerId}")
    @Transactional
    public String updateEmployer(
            @PathVariable Long employerId,
            @RequestBody EmployerAllDetailsUpdateDTO cashierAllDetailsUpdateDTO
    ) {
        String message = employerService.updateEmployer(employerId, cashierAllDetailsUpdateDTO);
        return message;
    }

    /**
     * Updates the account details of an employer.
     *
     * @param cashierUpdateAccountDetailsDTO DTO containing updated account details of the employer.
     * @return A string indicating the success of the operation.
     */
    @PutMapping("/updateAccountDetails")
    @Transactional
    public String updateEmployerAccountDetails(
            @RequestBody EmployerUpdateAccountDetailsDTO cashierUpdateAccountDetailsDTO
    ) {
        String message = employerService.updateEmployerAccountDetails(cashierUpdateAccountDetailsDTO);
        return message;
    }

    /**
     * Updates the bank account details of an employer.
     *
     * @param employerId                    The ID of the employer to update.
     * @param employerUpdateBankAccountDTO  The DTO containing the updated bank account details.
     * @return                              ResponseEntity containing the updated employer data
     *                                      along with bank account details,
     *                                      or an HTTP status indicating the failure if the employer is not found.
     */
    @PutMapping("/updateEmployerBankAccountDetailsWithId/{employerId}")
    @Transactional
    public ResponseEntity<StandardResponse> updateEmployerBankAccountDetailsWithId(
            @PathVariable long employerId,
            @RequestBody EmployerUpdateBankAccountDTO employerUpdateBankAccountDTO
    ) {
        try {
            EmployerWithBankDTO employerWithBankDTO = employerService
                    .updateEmployerBankAccountDetails(employerUpdateBankAccountDTO);
            EmployerBankDetailsDTO bankDetailsDTO = employerService
                    .getEmployerBankDetailsById(employerId);
            employerWithBankDTO.setEmployerBankDetails(
                    employerMapper.mapBankDetailsDTOToEntity(bankDetailsDTO)
            ); // Utilize the mapper// Map DTO to Entity
            return ResponseEntity.ok(
                    new StandardResponse(
                            201, "SUCCESS", employerWithBankDTO)
            );
        } catch (NotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            new StandardResponse(404, ex.getMessage(), null)
                    );
        }
    }

    /**
     * Retrieves an employer along with their bank details.
     * @param employerId The ID of the employer to retrieve.
     * @return ResponseEntity containing the employer DTO with bank details if found,
     *         or an HTTP status indicating the failure if the employer is not found.
     */
    @GetMapping("/getEmployerWithBankDetails/{employerId}")
    public ResponseEntity<StandardResponse> getEmployerWithBankDetails(@PathVariable long employerId) {
        // Retrieve employer DTO with bank details
        EmployerWithBankDTO employerWithBankDTO = employerService.getEmployerByIdWithBankDetails(employerId);

        // Check if the employer exists
        if (employerWithBankDTO != null) {
            // Return the employer DTO with bank details
            return new ResponseEntity<>(
                    new StandardResponse(201, "SUCCESS", employerWithBankDTO),
                    HttpStatus.OK
            );
        } else {
            // Return an HTTP status indicating the employer is not found
            return new ResponseEntity<>(
                    new StandardResponse(404, "Employer not found", employerWithBankDTO),
                    HttpStatus.NOT_FOUND
            );
        }

    }

    /**
     * Retrieves an employer by ID.
     *
     * @param employerId The ID of the employer to retrieve.
     * @return The EmployerDTO object representing the employer.
     */
    @GetMapping(path = "/get-by-id", params = "id")
    @Transactional
    public EmployerDTO getEmployerById(@RequestParam(value = "id") int employerId) {
        EmployerDTO employerDTO = employerService.getEmployerById(employerId);
        return employerDTO;
    }

    /**
     * Retrieves the image of an employer by ID.
     *
     * @param employerId The ID of the employer whose image is to be retrieved.
     * @return ResponseEntity containing the image byte array.
     */
    @GetMapping("/view-image/{employerId}")
    @Transactional
    public ResponseEntity<byte[]> viewImage(@PathVariable int employerId) {
        byte[] imageData = employerService.getImageData(employerId);

        if (imageData != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Adjust the media type based on your image format
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes an employer by ID.
     *
     * @param employerId The ID of the employer to delete.
     * @return A string indicating the success of the operation.
     */
    @DeleteMapping(path = "/delete-employerId/{id}")
    public String deleteEmployer(@PathVariable(value = "id") int employerId) {
        String deleted = employerService.deleteEmployer(employerId);
        return deleted;
    }

    /**
     * Retrieves all employers.
     *
     * @return ResponseEntity containing a list of all employers.
     */
    @GetMapping(path = "/get-all-employers")
    public ResponseEntity<StandardResponse> getAllEmployers() {
        List<EmployerDTO> allEmployer = employerService.getAllEmployer();
        return new ResponseEntity<StandardResponse>(
                new StandardResponse(201, "SUCCESS", allEmployer),
                HttpStatus.OK
        );
    }


    /**
     * Retrieves all employers.
     *
     * @return ResponseEntity containing a list of all employers.
     */
    @GetMapping(path = "/get-all-employers-by-active-state/{status}")
    @Transactional
    public List<EmployerDTO> getAllEmployerByActiveState(@PathVariable(value = "status") boolean activeState) {
        List<EmployerDTO> allemployer = employerService.getAllEmployerByActiveState(activeState);
        return allemployer;
    }

    /**
     * Retrieves bank details of all employers.
     *
     * @return ResponseEntity containing a StandardResponse object with a list of employer bank details.
     */
    @GetMapping(path = "/get-all-employers-bank-details")
    @Transactional
    public ResponseEntity<StandardResponse> getAllEmployerBankDetails() {
        List<EmployerUpdateBankAccountDTO> allCashiersBankDetails = employerService.getAllEmployerBankDetails();
        return new ResponseEntity<StandardResponse>(
                new StandardResponse(201, "SUCCESS", allCashiersBankDetails),
                HttpStatus.OK
        );
    }
}