<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }
?>
<section id="team" data-stellar-background-ratio="1">
      <div class="container">
           <div class="row">
                <div class="col-md-12 col-sm-12">
                     <div class="section-title wow fadeInUp">
                          <h2 class="wow fadeInUp" data-wow-delay="0.1s"><center>Dokter Kami</center></h2>
                     </div>
                </div>
                       
                <div class="clearfix"></div>
                <?php
                    if(!isset($_SESSION["semuadokter"])){
                        $datadokter       = "";
                        $querysemuadokter = bukaquery("select dokter.kd_dokter,left(dokter.nm_dokter,40) as dokter,spesialis.nm_sps from dokter inner join spesialis on dokter.kd_sps=spesialis.kd_sps inner join pegawai on dokter.kd_dokter=pegawai.nik where dokter.status='1' and dokter.kd_dokter<>'-' order by spesialis.nm_sps");
                        while($rsquerysemuadokter = mysqli_fetch_array($querysemuadokter)) {
                            $datadokter=$datadokter.
                                    "<div class='col-md-4 col-sm-6'>
                                        <div class='team-thumb wow fadeInUp' data-wow-delay='0.2s'>
                                           
                                              <div class='team-info'>
                                                   <h5>$rsquerysemuadokter[1]</h5>
                                                   <p>$rsquerysemuadokter[2]</p>
                                                   
                                              </div>
                                        </div>
                                        <br/>
                                   </div>";
                        }
                        $_SESSION["semuadokter"]=$datadokter;
                    }
                    echo $_SESSION["semuadokter"];
                ?>
           </div>
      </div>
 </section>
