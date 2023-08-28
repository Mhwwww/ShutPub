terraform {
  required_providers {
    google = {
      source = "hashicorp/google"
      version = "4.51.0"
    }
  }
}

provider "google" {
  project = "schirmer-project"
  region = "europe-west3"
  zone = "europe-west3-a"

  credentials = file(var.key_file_location)
}


#create a Virtual Private Cloud (VPC) network and subnet for the VM's network interface.
resource "google_compute_network" "vpc_network" {
  name = "psf-network"

  auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "broker" {
  name          = "psf-broker-subnetwork"
  ip_cidr_range = "10.1.2.0/24"
  network       = google_compute_network.vpc_network.id
}

resource "google_compute_subnetwork" "publisher" {
  name          = "psf-broker-subnetwork"
  ip_cidr_range = "10.2.2.0/24"
  region = "europe-southwest1"
  network       = google_compute_network.vpc_network.id
}

resource "google_compute_subnetwork" "subscriber" {
  name          = "psf-broker-subnetwork"
  ip_cidr_range = "10.3.2.0/24"
  region = "europe-west9"
  network       = google_compute_network.vpc_network.id
}

#Create the Compute Engine VM resource # Create a single Compute Engine instance
resource "google_compute_instance" "broker" {
  name         = "psf-broker-vm"
  machine_type = "e2-standard-2"
  zone = "europe-west3-a"//frankfurt
  tags = ["ssh", "app"]

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
    }
  }

  metadata = {
    ssh-keys="debian:${file(var.gce_ssh_pub_key_file)}"
  }

  network_interface {
    subnetwork = google_compute_subnetwork.broker.id
    access_config {
      # Include this section to give the VM an external IP address
    }
  }
}

resource "google_compute_instance" "subscriber" {
  name         = "psf-subscriber-vm"
  machine_type = "e2-standard-2"
  zone = "europe-west9-a"

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
    }
  }

  metadata = {
    ssh-keys = "debian:${file(var.gce_ssh_pub_key_file)}"


  }

  network_interface {
    subnetwork = google_compute_subnetwork.subscriber.id
    access_config {
      # Include this section to give the VM an external IP address
    }
  }
}

resource "google_compute_instance" "publisher" {
  name         = "psf-publisher-vm"
  machine_type = "e2-standard-2"
  zone = "europe-southwest1-b"

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
    }
  }

  metadata = {
    ssh-keys = "debian:${file(var.gce_ssh_pub_key_file)}"
  }

  network_interface {
    subnetwork = google_compute_subnetwork.publisher.id
    access_config {
      # Include this section to give the VM an external IP address
    }
  }
}


resource "google_compute_firewall" "ssh" {
  name = "allow-ssh"
  allow {
    protocol = "tcp"
    ports = ["22"]
  }
  direction = "INGRESS"
  network = google_compute_network.vpc_network.id
  source_ranges = ["0.0.0.0/0"]
  target_tags = ["ssh"]
}

resource "google_compute_firewall" "grpc_server" {
  name    = "grpc-app-firewall"
  network = google_compute_network.vpc_network.id

  allow {
    protocol = "tcp"
    ports    = ["61616"]
  }
  source_ranges = ["0.0.0.0/0"]
  target_tags = ["app"]
}