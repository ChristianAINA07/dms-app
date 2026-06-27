import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    MatFormFieldModule,
    MatDividerModule
  ],
  templateUrl: './app.html',
  styleUrls: []
})
export class AppComponent implements OnInit {
  /* États de session de l'utilisateur connecté */
  isLoggedIn = false;
  currentUserId: number | null = null;
  currentUsername = '';
  currentUserRole = ''; /* Contient ADMIN, MANAGER, ou EMPLOYE */

  /* Champs du formulaire de connexion */
  loginEmail = '';
  loginPassword = '';

  totalDocuments = 0;
  totalFolders = 0;
  documentsToday = 0;
  storageUsed = '0.00 Mo';
  
  folders: any[] = [];
  recentLogs: any[] = [];
  allDocuments: any[] = [];
  filteredDocuments: any[] = [];
  searchQuery = '';

  newFolderName = '';
  documentTitle = '';
  selectedFolderId: number | null = null;
  selectedFile: File | null = null;
  notificationMessage = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    /* L'application ne charge les données que si l'utilisateur est authentifié */
    if (this.isLoggedIn) {
      this.loadDashboardData();
      this.loadDocuments();
    }
  }

  /* Traitement de la soumission du formulaire de Login */
  onLogin(): void {
    const credentials = { email: this.loginEmail, password: this.loginPassword };
    this.http.post<any>('http://localhost:8081/api/login', credentials).subscribe({
      next: (res) => {
        if (res.status === 'SUCCESS') {
          this.isLoggedIn = true;
          this.currentUserId = res.userId;
          this.currentUsername = res.username;
          this.currentUserRole = res.role;
          this.showNotification(`Connexion réussie. Bienvenue ${this.currentUsername} !`);
          
          /* Chargement immédiat de l'environnement après authentification */
          this.loadDashboardData();
          this.loadDocuments();
        }
      },
      error: () => this.showNotification('Échec de connexion : Email ou mot de passe incorrect.')
    });
  }

  /* Déconnexion de la session active */
  onLogout(): void {
    this.isLoggedIn = false;
    this.currentUserId = null;
    this.currentUsername = '';
    this.currentUserRole = '';
    this.loginEmail = '';
    this.loginPassword = '';
  }

  loadDashboardData(): void {
    this.http.get<any>('http://localhost:8081/api/dashboard').subscribe({
      next: (data) => {
        this.totalDocuments = data.totalDocuments;
        this.totalFolders = data.totalFolders;
        this.documentsToday = data.documentsToday;
        this.storageUsed = data.storageUsed;
        this.folders = data.folders;
        this.recentLogs = data.recentLogs;
      }
    });
  }

  loadDocuments(): void {
    this.http.get<any[]>('http://localhost:8081/api/documents').subscribe({
      next: (docs) => {
        this.allDocuments = docs;
        this.applyFilter();
      }
    });
  }

  applyFilter(): void {
    if (!this.searchQuery.trim()) {
      this.filteredDocuments = this.allDocuments;
    } else {
      const query = this.searchQuery.toLowerCase().trim();
      this.filteredDocuments = this.allDocuments.filter(doc => 
        doc.title.toLowerCase().includes(query) || 
        doc.folderName.toLowerCase().includes(query)
      );
    }
  }

  onDownloadFile(versionId: number, originalTitle: string): void {
    const url = `http://localhost:8081/api/documents/download/${versionId}`;
    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const a = document.createElement('a');
        const objectUrl = URL.createObjectURL(blob);
        a.href = objectUrl;
        a.download = originalTitle;
        a.click();
        URL.revokeObjectURL(objectUrl);
      }
    });
  }

  /* NOUVELLE ACCESSIBILITÉ : SUPPRESSION DOCUMENT LINK BACKEND */
  onDeleteDocument(docId: number, title: string): void {
    if (confirm(`Voulez-vous vraiment supprimer définitivement le document "${title}" ?`)) {
      const url = `http://localhost:8081/api/documents/${docId}?userId=${this.currentUserId}`;
      this.http.delete(url).subscribe({
        next: () => {
          this.showNotification(`Le document "${title}" a été supprimé du système.`);
          this.loadDashboardData();
          this.loadDocuments();
        }
      });
    }
  }

  showNotification(msg: string): void {
    this.notificationMessage = msg;
    setTimeout(() => { this.notificationMessage = ''; }, 3000);
  }

  onCreateFolder(): void {
    if (!this.newFolderName.trim()) return;
    this.http.post('http://localhost:8081/api/folders', { name: this.newFolderName.trim() }).subscribe({
      next: () => {
        this.showNotification(`Le dossier "${this.newFolderName}" a été créé.`);
        this.newFolderName = '';
        this.loadDashboardData();
        this.loadDocuments();
      }
    });
  }

  onFileSelected(event: any): void {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
    }
  }

  onUploadDocument(): void {
    if (!this.selectedFile || !this.documentTitle.trim() || !this.selectedFolderId) return;

    const formData = new FormData();
    formData.append('file', this.selectedFile, this.selectedFile.name);
    formData.append('title', this.documentTitle.trim());
    formData.append('folderId', this.selectedFolderId.toString());
    formData.append('userId', this.currentUserId!.toString()); /* Traçabilité de l'opérateur connecté */

    this.http.post('http://localhost:8081/api/documents/upload', formData).subscribe({
      next: () => {
        this.showNotification(`Document enregistré.`);
        this.documentTitle = '';
        this.selectedFolderId = null;
        this.selectedFile = null;
        this.loadDashboardData();
        this.loadDocuments();
      }
    });
  }
}
